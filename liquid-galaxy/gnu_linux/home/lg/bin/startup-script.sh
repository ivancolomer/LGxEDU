#!/bin/bash

${HOME}/bin/ip-reloader.sh &

OCTET=$(cat ~/personavars.txt | grep DHCP_OCTET | sed 's/=/\ /g' | awk '{print $2}')
INTERFACE=$(cat ~/personavars.txt | grep DHCP_NETWORK_INTERFACE | sed 's/=/\ /g' | awk '{print $2}')

. ${HOME}/etc/shell.conf

MACHINE_ID=$(( ${FRAME_NO} + 1 ))

if ! [[ $DISPLAY == *"."* ]]; then
   DISPLAY="${DISPLAY}.0"
   export DISPLAY=$DISPLAY
fi

echo "Octet = $OCTET"
echo "Machine ID = $MACHINE_ID"
echo "Interface = $INTERFACE"
echo "MY FRAME = \"${FRAME_NO}\""
echo "DISPLAY = \"$DISPLAY\""
echo "DISPLAY_portion = \"${DISPLAY//*:}\""

if [[ $FRAME_NO = 0 ]]; then
    #nitrogen --set-zoom-fill ${XDG_PICTURES_DIR}/backgrounds/lg-bg-${FRAME_NO}.png &
    ${SCRIPDIR}/launch-earth.sh &
elif [[ $FRAME_NO -ge 1 ]]; then
    #nitrogen --set-zoom-fill ${XDG_PICTURES_DIR}/backgrounds/lg-bg-${FRAME_NO}.png &
    echo "Slave: LG$((FRAME_NO+1))"
else
    # will wait up to 9 seconds in increments of 3
    # to get an IP
    IP_WAIT=0

    #nitrogen --set-tiled ${XDG_PICTURES_DIR}/backgrounds/lg-bg-noframe.png &

    while [[ $IP_WAIT -le 9 ]]; do
        PRIMARY_IP="$(ip addr show dev wlan0 primary | awk '/inet\ / { print $2}')"
        if [[ -z $PRIMARY_IP ]]; then
            let IP_WAIT+=3
            sleep 3
        else
            break
        fi
    done

    xmessage \
"\"Personality\" assignment is _essential_
My primary IP address: $PRIMARY_IP

Utilize ${HOME}/bin/personality.sh with root priv."
fi

