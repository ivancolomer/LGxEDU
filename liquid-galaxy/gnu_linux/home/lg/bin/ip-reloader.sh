#!/bin/bash

me=$$
ps -ef | grep 'ip-reloader.sh' | awk -v me=$me '$2 != me {print $2}' | xargs kill

OCTET=$(cat ~/personavars.txt | grep DHCP_OCTET | sed 's/=/\ /g' | awk '{print $2}')
MACHINE_ID=$(cat ~/personavars.txt | grep DHCP_LG_SCREEN\= | sed 's/=/\ /g' | awk '{print $2}' | sed 's/\"//g')
INTERFACE=$(cat ~/personavars.txt | grep DHCP_NETWORK_INTERFACE | sed 's/=/\ /g' | awk '{print $2}')

. ${HOME}/etc/shell.conf

MACHINE_ID=$(( ${FRAME_NO} + 1 ))


while true; do

    if [[ -z $(ps -A | grep sshd) ]]; then
        sudo /etc/init.d/ssh restart
    fi

    sudo iptables -P INPUT ACCEPT
    sudo iptables -P OUTPUT ACCEPT
    sudo iptables -P FORWARD ACCEPT
    sudo iptables -F

    if [[ -z $(ip addr | grep 10.42) ]]; then
        INTERFACE_CONNECTED=$(route -n | grep "^0.0.0.0"| head -1 | rev | cut -d' ' -f1 | rev)
        sudo ip addr add 10.42.$OCTET.$MACHINE_ID/24 dev $INTERFACE_CONNECTED
    fi

    sleep 3

done


