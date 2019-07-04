#!/bin/bash

#
# Google Earth perfect full screen (hides top white menu)
# https://groups.google.com/d/msg/liquid-galaxy/7yK4BDstM3o/t8WxCbvMgRAJ
# Installs Openbox, a windows manager which unlike Gnome 
# makes moving a window below negative vertical axis possible
# Devilspie will automatically detect when Earth window is
# open and move it to the right coordinates.
#

echo "Installing..."
sudo apt-get install -qq openbox obconf obmenu menu devilspie > /dev/null

printf "[Desktop Entry]\nEncoding=UTF-8\nName=LG-FullScreen\nGenericName=LiquidGalaxy full-screen\nComment=This script turns full-screen on\nExec=bash $HOME/earth/scripts/run-devilspie.sh\nTerminal=false\nOnlyShowIn=GNOME\nType=Application\nStartupNotify=false\nX-GNOME-Autostart-enabled=true\n" > $HOME"/.config/autostart/ds.desktop"

printf "[Desktop Entry]\nEncoding=UTF-8\nName=LG-OpenBox\nGenericName=LiquidGalaxy openbox\nComment=This script starts openbox\nExec=openbox --replace\nTerminal=false\nOnlyShowIn=GNOME\nType=Application\nStartupNotify=false\nX-GNOME-Autostart-enabled=true\n" > $HOME"/.config/autostart/openbox.desktop"

sed -i "s/\(wasFullScreen *= *\).*/\1false/" $HOME/earth/config/master/GoogleEarthPro.conf
sed -i "s/\(wasFullScreen *= *\).*/\1false/" $HOME/earth/config/slave/GoogleEarthPro.conf
