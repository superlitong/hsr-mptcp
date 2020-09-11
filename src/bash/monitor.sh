#!/bin/bash

list_iface=()
list_carrier=()
list_pre=()
list_cur=()
list_speed=()

function get_iface
{
    for iface in `ls /sys/class/net/ | grep wlan`; do
	if [ `cat /sys/class/net/${iface}/operstate` = "up" ]; then
	    list_iface+=(${iface})
	fi
    done
}

function get_carrier
{
    for iface in ${list_iface[@]}; do
	carrier=`iwconfig ${iface} | grep ESSID | cut -d '"' -f2 | awk '{print $1 }'`
	list_carrier+=(${carrier})
    done
}

function monitor
{
    j=0
    for iface in ${list_iface[@]}; do
	list_pre[j]=`cat /sys/class/net/${iface}/statistics/rx_bytes`
	(( j++ ))
    done

    sleep 1

    j=0
    for iface in ${list_iface[@]}; do
	list_cur[j]=`cat /sys/class/net/${iface}/statistics/rx_bytes`
	list_speed[j]=${list_cur[j]}-${list_pre[j]}
	(( list_speed[j] /= 1024 ))

	list_pre=${list_cur}
	(( j++ ))
    done
}

get_iface
get_carrier

clear

while true; do
    monitor
    j=0
    for carrier in ${list_carrier[@]}; do
	echo -ne "${list_speed[j]}KB/s (${list_carrier[j]} - ${list_iface[j]})     "
	(( j++ ))
    done
    echo -ne "\033[0K\r"
done
