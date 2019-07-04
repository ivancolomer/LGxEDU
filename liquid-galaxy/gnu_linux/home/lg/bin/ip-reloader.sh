#!/bin/bash

me=$$
ps -ef | grep 'ip-reloader.sh' | awk -v me=$me '$2 != me {print $2}' | xargs kill

OCTET=$(cat ~/personavars.txt | grep DHCP_OCTET | sed 's/=/\ /g' | awk '{print $2}')
MACHINE_ID=$(cat ~/personavars.txt | grep DHCP_LG_SCREEN\= | sed 's/=/\ /g' | awk '{print $2}' | sed 's/\"//g')
INTERFACE=$(cat ~/personavars.txt | grep DHCP_NETWORK_INTERFACE | sed 's/=/\ /g' | awk '{print $2}')

. ${HOME}/etc/shell.conf

MACHINE_ID=$(( ${FRAME_NO} + 1 ))
LAST_TIME=$(date +%s)

while true; do

    if [[ -z $(ps -A | grep sshd) ]]; then
        sudo /etc/init.d/ssh restart
        echo 'ssh restarted'
    fi

    if [[ -z $(ps -A | grep squid) ]]; then
        sudo /etc/init.d/squid restart
        echo 'squid restarted'
    fi

    if [[ $FRAME_NO = 0 ]]; then
        if [[ -z $(ps -A | grep apache2) ]]; then
            sudo /etc/init.d/apache2 restart
            echo 'apache2 restarted'
        fi
    fi

    if [[ -z $(sudo iptables -S | grep 10.42. |  head -1) ]]; then
        sudo tee "/etc/iptables.conf" > /dev/null << EOM
*filter
:INPUT ACCEPT [0:0]
:FORWARD ACCEPT [0:0]
:OUTPUT ACCEPT [43616:6594412]
-A INPUT -i lo -j ACCEPT
-A INPUT -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -p icmp -j ACCEPT

#-A INPUT -p tcp -m multiport --dports 22 -j ACCEPT
-A INPUT -p tcp -m tcp --dport 22 -j ACCEPT

-A INPUT -s 10.42.0.0/16 -p udp -m udp --dport 161 -j ACCEPT
-A INPUT -s 10.42.0.0/16 -p udp -m udp --dport 3401 -j ACCEPT

#-A INPUT -p tcp -m multiport --dports 81,8111 -j ACCEPT
-A INPUT -p tcp -m tcp --dport 81 -j ACCEPT
-A INPUT -p tcp -m tcp --dport 8111 -j ACCEPT

#-A INPUT -s 10.42.$OCTET.0/24 -p tcp -m multiport --dports 80,3128,3130 -j ACCEPT
-A INPUT -s 10.42.$OCTET.0/24 -p tcp -m tcp --dport 80 -j ACCEPT
-A INPUT -s 10.42.$OCTET.0/24 -p tcp -m tcp --dport 3128 -j ACCEPT
-A INPUT -s 10.42.$OCTET.0/24 -p tcp -m tcp --dport 3130 -j ACCEPT

#-A INPUT -s 10.42.$OCTET.0/24 -p udp -m multiport --dports 80,3128,3130 -j ACCEPT
-A INPUT -s 10.42.$OCTET.0/24 -p udp -m udp --dport 80 -j ACCEPT
-A INPUT -s 10.42.$OCTET.0/24 -p udp -m udp --dport 3128 -j ACCEPT
-A INPUT -s 10.42.$OCTET.0/24 -p udp -m udp --dport 3130 -j ACCEPT

#-A INPUT -s 10.42.$OCTET.0/24 -p tcp -m multiport --dports 9335 -j ACCEPT
-A INPUT -s 10.42.$OCTET.0/24 -p tcp -m tcp --dport 9335 -j ACCEPT

-A INPUT -s 10.42.$OCTET.0/24 -d 10.42.$OCTET.255/32 -p udp -j ACCEPT
-A INPUT -j DROP
-A FORWARD -j DROP
COMMIT
*nat
:PREROUTING ACCEPT [52902:8605309]
:INPUT ACCEPT [0:0]
:OUTPUT ACCEPT [358:22379]
:POSTROUTING ACCEPT [358:22379]
COMMIT
EOM

        sudo iptables-restore < /etc/iptables.conf
        echo 'iptables restarted'
    fi

    if [[ -z $(ip addr | grep 10.42.) ]]; then
        INTERFACE_CONNECTED=$(route -n | grep "^0.0.0.0" | head -1 | rev | cut -d' ' -f1 | rev)
        sudo ip addr add 10.42.$OCTET.$MACHINE_ID/24 dev $INTERFACE_CONNECTED
        echo 'subnet restarted'
    fi

    if [ "$(date +%s)" -gt "$(($LAST_TIME))" ]; then
        xdotool key 'Escape'
        LAST_TIME=$(( $(date +%s) + 120 )) #every 2minutes ESC key will be pressed to make chromebooks not sleep
    fi

    sleep 3

done

