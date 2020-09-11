#!/bin/bash

filename="throughput.log"
mainfilename="mainthroughput.log"
iface="eth0"

while [ -n "$1" ]; do
    case $1 in
	-o | --output    ) shift; filename=$1
	                   ;;
	-i | --interface ) shift; iface=$1
	                   ;;
	*                ) ;;
    esac
    shift
done

while true; do
    rx_pkt=`cat /sys/class/net/${iface}/statistics/rx_packets`
    rx_byte=`cat /sys/class/net/${iface}/statistics/rx_bytes`
    tx_pkt=`cat /sys/class/net/${iface}/statistics/tx_packets`
    tx_byte=`cat /sys/class/net/${iface}/statistics/tx_bytes`
    echo $(date "+%s.%N"):${rx_pkt}:${rx_byte}:${tx_pkt}:${tx_byte} >> ${filename}
    sleep 0.01s
done
