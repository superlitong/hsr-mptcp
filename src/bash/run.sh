#!/bin/bash

PATH_FL="./settings/filelist.txt"
PATH_CC="./settings/congestion_control.txt"
PATH_BUF="./settings/buffer_size.txt"
PATH_WBUF="./settings/wbuffer_size.txt"
PATH_SCH="./settings/scheduler.txt"
PATH_DIR="./settings/directory.txt"

id=
carr_type=0
expr_type=0
count_mobile=0
count_unicom=0
count_telcom=0

list_iface=()
list_carrier=()
list_fl=()
list_cc=()
list_buf=()
list_wbuf=()
list_sch=()

function get_iface_and_carrier
{
    for iface in `ls /sys/class/net/ | grep wlan`; do
	if [ `cat /sys/class/net/${iface}/operstate` = "up" ]; then
	    list_iface+=(${iface})
	    carrier=`iwconfig ${iface} | grep ESSID | cut -d':' -f2 | cut -d'"' -f2`
	    list_carrier+=(${carrier})
	    case "${carrier}" in
		*Mobile* | *mobile* ) (( count_mobile++ )) ;;
		*Unicom* | *unicom* ) (( count_unicom++ )) ;;
		*Telcom* | *telcom* ) (( count_telcom++ )) ;;
	    esac
	fi
    done
    echo "UP interfaces: ${list_iface[@]}"
    echo "Carrier: ${list_carrier[@]}"
}

# @param $1 - seconds to count down
# @param $2 - tip information
function countdown
{
    seconds=${1}
    while [[ ${seconds} -ge 0 ]]; do
	echo -ne "The ${2} will start in ${seconds} seconds...\033[0K\r"
	(( seconds -= 1 ))
	sleep 1
    done
}

function get_carrier_type
{
    string="There are %s interfaces connect to *%s*\nPlease check your interfaces and restart the program\n"
    
    if [[ "${count_mobile}" -gt 1 ]]; then
	printf "$string" ${count_mobile} "CHINA MOBILE"; exit
    elif [[ "${count_unicom}" -gt 1 ]]; then
	printf "$string" ${count_unicom} "CHINA UNICOM"; exit
    elif [[ "${count_telcom}" -gt 1 ]]; then
	printf "$string" ${count_telcom} "CHINA TELCOM"; exit
    else
	(( carr_type = count_mobile * 4 + count_unicom * 2 + count_telcom ))
	if [[ "${carr_type}" = 0 ]]; then
	    echo "There's no WiFi connection. Please check your connection and try again."
	    exit;
	fi
	echo carrier type = ${carr_type}
    fi
}

# @param $1 - file size 
# @param $2 - congestion control
# @param $3 - buffer size
# @param $4 - scheduler
# @param $5 - wbuffer size
function get_expr_type
{
    expr_type=0
    
    case ${5} in
	1048576    )   (( expr_type += 1 )) ;;
	6291456   )   (( expr_type += 2 )) ;;
	11534336     )   (( expr_type += 3 )) ;;
	16777216    )   (( expr_type += 4 )) ;;
	# 200M.dat   )   (( expr_type += 5 )) ;;
    esac
    
    case ${2} in
	lia        )   (( expr_type += 0 )) ;;
	olia       )   (( expr_type += 5 )) ;;
	reno       )   (( expr_type += 10 ));;
    esac

    case ${3} in
	1048576    )   (( expr_type += 20 )) ;;
	6291456   )   (( expr_type += 30 )) ;;
	11534336     )   (( expr_type += 40 )) ;;
	16777216    )   (( expr_type += 50 )) ;;
    esac

    case ${4} in
	default    )   (( expr_type += 0 )) ;;
	roundrobin )   (( expr_type += 15 ));;
    esac
}

# @param $1 - the root directory path
# @param $2 - the train ID, for example C2008, G234, and so on
function load_env
{
    train_path=${1}/${2}
    if [[ -e "$2" ]]; then
	id=`ls -l ${train_path} | grep "^d" | wc -l`
	(( id++ ))
	echo "current id is ${id}"
    else
	id=1
	mkdir -p ${train_path}
        train_st_time=`date +%s.%N`
	echo ${train_st_time} > ${train_path}/train_start_time
	echo `date +%s.%N` > ${train_path}/train_end_time
    fi

    sudo ifconfig wlan0 down
    echo ${train_st_time} $2 ${train_path} ${train_path}/global.log >> total.log

    get_iface_and_carrier
#    get_carrier_type
    echo ${carr_type} > ${1}/${2}/carrier_type

    IFS=$'\n' list_fl=($(< ${PATH_FL}))
    IFS=$'\n' list_cc=($(< ${PATH_CC}))
    IFS=$'\n' list_buf=($(< ${PATH_BUF}))
    IFS=$'\n' list_wbuf=($(<${PATH_WBUF}))
#    IFS=$'\n' list_sch=($(< ${PATH_SCH}))


    cc=${list_cc[ $RANDOM % ${#list_cc[@]} ] }
    buf=${list_buf[ $RANDOM % ${#list_buf[@]} ] }
    wbuf=${list_wbuf[ $RANDOM % ${#list_wbuf[@]} ]}
    sch=""
#    sch=${list_sch[ $RANDOM % ${#list_sch[@]} ] }

    echo "server -------->"
    ssh root@${SERVERIP} sysctl -w net.ipv4.tcp_congestion_control=${cc}
    ssh root@${SERVERIP} sysctl -w net.ipv4.tcp_wmem="\"${wbuf} ${wbuf} ${wbuf}\""
#    ssh root@${SERVERIP} sysctl -w net.mptcp.mptcp_scheduler=${sch}

    echo "client -------->"
    sudo sysctl -w net.ipv4.tcp_congestion_control=${cc}
    sudo sysctl -w net.ipv4.tcp_rmem="${buf} ${buf} ${buf}"
#    sudo sysctl -w net.mptcp.mptcp_scheduler=${sch}
}

# @param $1 - file name
function create_random_file
{
    src=/dev/urandom
    dst=/var/www/blocks
    
    case $1 in
	600M )
	    ssh root@${SERVERIP} dd if=${src} of=${dst}/600M.dat bs=20MB count=30
	    ;;
	* )
	    ssh root@${SERVERIP} dd if=${src} of=${dst}/${1} bs=${1%%.*}B count=1
	    ;;
    esac
}

# @param $1 - download time
function killwget
{
    sleep $1
    sudo pkill wget
} 

# @param $1 - time gap between two tests
function control_time
{
    test_date=`date "+%M%S"`
    ctl_time=$[ $1*100 ]
    clear
    echo "Waiting for Test..."
    while (( `expr $test_date % $ctl_time` != 0 ))
    do
        test_date=`date "+%M%S"`
    done

}

# @param $1 - train directory path
function test
{
    clear
    start_time=`date +%s.%N`
    end_time=0

    get_expr_type ${file} ${cc} ${buf} ${sch} ${wbuf}

    echo "Prepare to start experiment #${id}"
    echo
    echo "file = ${file}"
    echo "congestion control = ${cc}"
    echo "buffer size = ${buf}"
    echo "wbuffer size = ${wbuf}"
    echo "scheduler = ${sch}"
    echo "experiment type = ${expr_type}"
    
    client_path=${1}/${id}.${start_time}
    
    mkdir -p ${client_path}
    echo ${cc}  > ${client_path}/congestion_control
    echo ${buf} > ${client_path}/buffer_size
    echo ${wbuf} > ${client_path}/wbuffer_size
    echo ${sch} > ${client_path}/scheduler
    echo ${expr_type} > ${client_path}/expr_type
    echo ${start_time} > ${client_path}/start_time

    server_path="/root"
    server_pcap_path=${server_path}/server.${id}.${carr_type}.${expr_type}.eth1.${start_time}.pcap
    echo ${server_pcap_path} > ${1}/server_pcap_path
    echo ${SERVERIP} > ${1}/server_ip

# ==========================================================

    # create_random_file ${file}
    ssh root@${SERVERIP} tcpdump tcp -U -i eth1 -w ${server_pcap_path} &

    for iface in ${list_iface[@]}; do
	echo ${iface}
	carrier=`iwconfig ${iface} | grep ESSID | cut -d':' -f2 | cut -d '"' -f2 | cut -d' ' -f1`
	iface_path=${client_path}/${iface}
	iface_file_name=${id}.${carr_type}.${expr_type}.${carrier}.${start_time}
	client_pcap_path=${iface_path}/client.${iface_file_name}.pcap
	throughput_path=${iface_path}/${iface_file_name}.thp

	mkdir -p ${iface_path}
	echo `ifconfig ${iface} | grep 'inet addr:' | cut -d':' -f2 | awk '{ print $1 }'` > ${iface_path}/ip_address
	echo ${carrier} > ${iface_path}/carrier
	echo ${iface} > ${iface_path}/interface
	
	sudo tcpdump tcp -U -i ${iface} -w ${client_pcap_path} &
	sudo ./loop.sh -i ${iface} -o ${throughput_path} &
    done
    
    control_time 3
    countdown 15 "download"
    killwget 120&
    file=${list_fl[ $RANDOM % ${#list_fl[@]} ] }
    start_time=`date +%s.%N`
    wget ${SERVERIP}/blocks/${file}".dat"
    dwnload_size=`du -b ${file}".dat" | cut -f1`
    echo ${dwnload_size} > ${client_path}/download_size
    rm ${file}".dat"

    end_time=`date +%s.%N`
    echo ${end_time} > ${client_path}/end_time

    ssh root@${SERVERIP} pkill tcpdump
    sudo pkill tcpdump
    sudo pkill loop.sh
    for iface in ${list_iface[@]}; do
	carrier=`cat ${client_path}/${iface}/carrier`
	iface_path=${client_path}/${iface}
	iface_file_name=${id}.${carr_type}.${expr_type}.${carrier}.${start_time}
	client_pcap_path=${iface_path}/client.${iface_file_name}.pcap
	throughput_path=${iface_path}/client.${iface_file_name}.thp

	echo ${id} ${carr_type} ${expr_type} ${cc} ${buf} ${wbuf} ${sch} ${file} ${SERVERIP} ${start_time} ${end_time} ${client_pcap_path} ${throughput_path} ${server_pcap_path} ${carrier} ${dwnload_size} >> ${1}/global.log

    done
    (( id++ ))
    echo ${end_time} > ${1}/train_end_time
}

function finish
{
    ssh root@${SERVERIP} pkill tcpdump
    sudo pkill tcpdump
    sudo pkill loop.sh

    if [[ -n "`pgrep wget`" ]]; then
	for iface in ${list_iface[@]}; do
	    carrier=`cat ${client_path}/${iface}/carrier`
	    iface_path=${client_path}/${iface}
	    iface_file_name=${id}.${carr_type}.${expr_type}.${iface}.${start_time}
	    client_pcap_path=${iface_path}/client.${iface_file_name}.pcap
	    throughput_path=${iface_path}/client.${iface_file_name}.thp
	    echo ${id} ${carr_type} ${expr_type} ${start_time} 0 ${client_pcap_path} ${throughput_path} ${server_pcap_path} ${carrier} >> ${1}/global.log
	done
    fi

    exit
}

#--------------------------------------------------------------------------------------------------------------------------
SERVERIP=123.57.183.78
root_dir="."

#trap finish SIGINT

if [[ -z $1 ]]; then
    echo "Please input current HSR ID and try again"
    exit
fi

echo "Checking time..."
sudo ntpdate time-a.nist.gov
load_env ${root_dir} "${1}"

while : ; do
    countdown 5 "next test"   

    test ${root_dir}/${1}
    
done
