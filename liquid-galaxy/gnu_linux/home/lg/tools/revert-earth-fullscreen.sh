#!/bin/bash

#
# Google Earth perfect full screen (hides top white menu)
#

echo "Removing..."
sudo apt-get remove --purge -qq openbox obconf obmenu devilspie > /dev/null
rm $HOME"/.config/autostart/ds.desktop"
rm $HOME"/.config/autostart/openbox.desktop"
sed -i "s/\(wasFullScreen *= *\).*/\1true/" $HOME/earth/config/master/GoogleEarthPlus.conf-7.1.2
sed -i "s/\(wasFullScreen *= *\).*/\1true/" $HOME/earth/config/slave/GoogleEarthPlus.conf-7.1.2
