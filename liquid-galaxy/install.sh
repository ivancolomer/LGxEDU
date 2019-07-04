#!/bin/bash

GIT_URL="https://github.com/ivancolomer/LGxEDU/"

cat << "EOM"
 _ _             _     _               _                  
| (_) __ _ _   _(_) __| |   __ _  __ _| | __ ___  ___   _ 
| | |/ _` | | | | |/ _` |  / _` |/ _` | |/ _` \ \/ / | | |
| | | (_| | |_| | | (_| | | (_| | (_| | | (_| |>  <| |_| |
|_|_|\__, |\__,_|_|\__,_|  \__, |\__,_|_|\__,_/_/\_\\__, |
        |_|                |___/                    |___/ 
https://github.com/ivancolomer/LGxEDU
-------------------------------------------------------------

EOM

# Parameters
IS_MASTER=false

MASTER_IP=""
MASTER_USER=$USER
MASTER_HOME=$HOME
MASTER_PASSWORD=""

LOCAL_USER=$USER
MACHINE_ID="1"
MACHINE_NAME="lg"$MACHINE_ID
TOTAL_MACHINES="3"

LG_FRAMES="lg3 lg1 lg2"
OCTET="42"
SCREEN_ORIENTATION="V"
GIT_FOLDER_MAIN="LGxEDU"
GIT_FOLDER_NAME="$GIT_FOLDER_MAIN/liquid-galaxy"

EARTH_FOLDER="/opt/google/earth/pro/"
NETWORK_INTERFACE="wlan0"
#$(/sbin/route -n | grep "^0.0.0.0"| head -1 | rev | cut -d' ' -f1 | rev)

read -p "Machine id (i.e. 1 for lg1) (1 == master): " MACHINE_ID
if [ "$(echo $MACHINE_ID | cut -c-2)" == "lg" ]; then
	MACHINE_ID="$(echo $MACHINE_NAME | cut -c3-)"
fi

MACHINE_NAME="lg"$MACHINE_ID

if [ $MACHINE_ID == "1" ]; then
	IS_MASTER=true
else
	echo "Make sure Master machine (lg1) is connected to the network before proceding!"
	read -p "Master machine IP (i.e. 192.168.1.42): " MASTER_IP
	read -p "Master local user password (i.e. lg password): " MASTER_PASSWORD
fi
read -p "Total machines count (i.e. 3): " TOTAL_MACHINES
read -p "Unique number that identifies your Galaxy (octet) (i.e. 42): " OCTET

#
# Pre-start
#

PRINT_IF_NOT_MASTER=""
if [ $IS_MASTER == false ]; then
	PRINT_IF_NOT_MASTER=$(cat <<- EOM

	MASTER_IP: $MASTER_IP
	MASTER_USER: $MASTER_USER
	MASTER_HOME: $MASTER_HOME
	MASTER_PASSWORD: $MASTER_PASSWORD
	EOM
	)
fi

mid=$((TOTAL_MACHINES / 2))

array=()

for j in `seq $((mid + 2)) $TOTAL_MACHINES`;
do
    array+=("lg"$j)
done

for j in `seq 1 $((mid+1))`;
do
    array+=("lg"$j)
done

cat << EOM

Liquid Galaxy will be installed with the following configuration:
MASTER: $IS_MASTER
LOCAL_USER: $LOCAL_USER
MACHINE_ID: $MACHINE_ID
MACHINE_NAME: $MACHINE_NAME $PRINT_IF_NOT_MASTER
TOTAL_MACHINES: $TOTAL_MACHINES
OCTET (UNIQUE NUMBER): $OCTET

GIT_URL: $GIT_URL 
GIT_FOLDER: $GIT_FOLDER_NAME

EARTH_FOLDER: $EARTH_FOLDER

Is it correct? Press any key to continue or CTRL-C to exit
EOM
read

if [ "$(cat /etc/os-release | grep 'PRETTY_NAME=\"Ubuntu 16.04.6 LTS\"')" == "" ]; then
	echo "Warning!! This script is meant to be run on an Ubuntu 16.04.6 LTS. It may not work as expected."
	echo -n "Press any key to continue or CTRL-C to exit"
	read
fi

if [[ $EUID -eq 0 ]]; then
   echo "Do not run it as root!" 1>&2
   exit 1
fi

# Initialize sudo access
sudo -v

#
# General
#

# export DEBIAN_FRONTEND=noninteractive

# Update OS
echo "Checking for system updates..."
sudo apt-get update

echo "Upgrading system packages ..."
sudo apt-get -yq upgrade

echo "Installing new packages..."
sudo apt-get install -yq tcpdump chromium-browser nano git openssh-server sshpass squid squid3 squid-cgi apache2 xdotool unclutter zip wish iptables bc lsb-core lsb iputils-ping
sudo apt-get install -yq libglib2.0-bin libfontconfig1 libx11-6 libxrender1 libxext6 libglu1-mesa libglib2.0-0 libsm6

#
# Liquid Galaxy
#

# Setup Liquid Galaxy files
echo "Setting up Liquid Galaxy..."
git clone $GIT_URL

echo "Installing Google Earth..."
sudo dpkg -i $GIT_FOLDER_NAME/google-earth-pro-stable_7.1.8.3036-r0_amd64.deb
sudo apt-get -f install -y

sudo cp -r $GIT_FOLDER_NAME/earth/ $HOME
sudo ln -s $EARTH_FOLDER $HOME/earth/builds/latest
#sudo ln -s /opt/google/earth/pro/drivers.ini $HOME/earth/builds/latest/drivers.ini
awk '/LD_LIBRARY_PATH/{print "export LC_NUMERIC=en_US.UTF-8"}1' $HOME/earth/builds/latest/googleearth | sudo tee $HOME/earth/builds/latest/googleearth > /dev/null

# Enable solo screen for slaves
if [ $IS_MASTER != true ]; then
	sudo sed -i -e 's/slave_x/slave_'${MACHINE_ID}'/g' $HOME/earth/kml/slave/myplaces.kml
	sudo sed -i -e 's/sync_nlc_x/sync_nlc_'${MACHINE_ID}'/g' $HOME/earth/kml/slave/myplaces.kml
fi

sudo cp -r $GIT_FOLDER_NAME/gnu_linux/home/lg/. $HOME
sudo chmod -R u+x $HOME/bin
sudo chmod -R u+x $HOME/tools

cd $HOME"/dotfiles/"
for file in *; do
    sudo mv "$file" ".$file"
done
sudo cp -r . $HOME
cd - > /dev/null

sudo cp -r $GIT_FOLDER_NAME/gnu_linux/etc/ $GIT_FOLDER_NAME/gnu_linux/patches/ $GIT_FOLDER_NAME/gnu_linux/sbin/ / 

sudo chmod 0440 /etc/sudoers.d/42-lg
sudo chown -R $LOCAL_USER:$LOCAL_USER $HOME
sudo chown $LOCAL_USER:$LOCAL_USER /home/lg/earth/builds/latest/drivers.ini

# Configure SSH
if [ $IS_MASTER == true ]; then
	echo "Setting up SSH..."
	$HOME/tools/clean-ssh.sh
else
	echo "Starting SSH files sync with master..."
	sshpass -p "$MASTER_PASSWORD" scp -o StrictHostKeyChecking=no $MASTER_IP:$MASTER_HOME/ssh-files.zip $HOME/
	unzip $HOME/ssh-files.zip -d $HOME/ > /dev/null
	sudo cp -r $HOME/ssh-files/etc/ssh /etc/
	sudo cp -r $HOME/ssh-files/root/.ssh /root/ 2> /dev/null
	sudo cp -r $HOME/ssh-files/user/.ssh $HOME/
	sudo rm -r $HOME/ssh-files/
	sudo rm $HOME/ssh-files.zip
fi
sudo chmod 0600 $HOME/.ssh/lg-id_rsa
sudo chmod 0600 /root/.ssh/authorized_keys
sudo chmod 0600 /etc/ssh/ssh_host_dsa_key
sudo chmod 0600 /etc/ssh/ssh_host_ecdsa_key
sudo chmod 0600 /etc/ssh/ssh_host_rsa_key
sudo chown -R $LOCAL_USER:$LOCAL_USER $HOME/.ssh

# prepare SSH files for other nodes (slaves)
if [ $IS_MASTER == true ]; then
	mkdir -p ssh-files/etc
	sudo cp -r /etc/ssh ssh-files/etc/
	mkdir -p ssh-files/root/
	sudo cp -r /root/.ssh ssh-files/root/ 2> /dev/null
	mkdir -p ssh-files/user/
	sudo cp -r $HOME/.ssh ssh-files/user/
	sudo zip -FSr "ssh-files.zip" ssh-files
	if [ $(pwd) != $HOME ]; then
		sudo mv ssh-files.zip $HOME/ssh-files.zip
	fi
	sudo chown -R $LOCAL_USER:$LOCAL_USER $HOME/ssh-files.zip
	sudo rm -r ssh-files/
fi

# Screens configuration
cat > $HOME/personavars.txt << EOF
DHCP_LG_FRAMES="$LG_FRAMES"
DHCP_LG_FRAMES_MAX=$TOTAL_MACHINES

FRAME_NO=$(cat /home/lg/frame 2>/dev/null)
DHCP_LG_SCREEN="$(( ${FRAME_NO} + 1 ))"
DHCP_LG_SCREEN_COUNT=1
DHCP_OCTET=$OCTET
DHCP_LG_PHPIFACE="http://lg1:81/"
DHCP_NETWORK_INTERFACE=$NETWORK_INTERFACE

DHCP_EARTH_PORT=45678
DHCP_EARTH_BUILD="latest"
DHCP_EARTH_QUERY="/tmp/query.txt"

DHCP_MPLAYER_PORT=45680
EOF

sudo $HOME/bin/personality.sh $MACHINE_ID $OCTET > /dev/null

# Network configuration

sudo tee "/etc/network/interfaces" > /dev/null 2>&1 << EOM
#interfaces used by if-up and if-down

auto wlan0 
iface wlan0 inet dhcp
post-up ip addr add 10.42.$OCTET.$MACHINE_ID/24 dev wlan0
down ip addr del 10.42.$OCTET.$MACHINE_ID/24 dev wlan0

auto eth0 
iface eth0 inet dhcp
post-up ip addr add 10.42.$OCTET.$MACHINE_ID/24 dev eth0
down ip addr del 10.42.$OCTET.$MACHINE_ID/24 dev eth0

EOM

sudo sed -i '/lgX.liquid.local/d' /etc/hosts
sudo sed -i '/kh.google.com/d' /etc/hosts
sudo sed -i '/10.42./d' /etc/hosts
sudo tee -a "/etc/hosts" > /dev/null 2>&1 << EOM
10.42.$OCTET.1  lg1
10.42.$OCTET.2  lg2
10.42.$OCTET.3  lg3
10.42.$OCTET.4  lg4
10.42.$OCTET.5  lg5
10.42.$OCTET.6  lg6
10.42.$OCTET.7  lg7
10.42.$OCTET.8  lg8
EOM
sudo sed -i '/10.42./d' /etc/hosts.squid
sudo tee -a "/etc/hosts.squid" > /dev/null 2>&1 << EOM
10.42.$OCTET.1  lg1
10.42.$OCTET.2  lg2
10.42.$OCTET.3  lg3
10.42.$OCTET.4  lg4
10.42.$OCTET.5  lg5
10.42.$OCTET.6  lg6
10.42.$OCTET.7  lg7
10.42.$OCTET.8  lg8
EOM

# Allow iptables to forward and recieve traffic
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

# If master, enable ssh daemon on startup
if [ $IS_MASTER == true ]; then
	sudo systemctl enable ssh
fi

sudo systemctl enable squid

# In-session ssh daemon start
sudo /etc/init.d/ssh start
sudo /etc/init.d/squid start

sudo chmod 0755 -R "$HOME"/bin/
sudo chmod 0755 -R "$HOME"/earth/scripts
sudo chmod 777 "$HOME"/bin/startup-script.sh

# Launch on boot
mkdir -p $HOME/.config/autostart/
printf "[Desktop Entry]\nEncoding=UTF-8\nName=LG\nGenericName=LiquidGalaxy launcher\nComment=This script initializes google earth\nExec=bash $HOME/bin/startup-script.sh\nTerminal=false\nOnlyShowIn=GNOME\nType=Application\nStartupNotify=false\nX-GNOME-Autostart-enabled=true\n" > $HOME"/.config/autostart/lg.desktop"

# Added screensaver off
cat >"$HOME"/bin/screen-saver-off.sh <<EOF
#!/bin/bash
sleep 10 &&
xset s 0 0
xset s off

gsettings set org.gnome.desktop.screensaver idle-activation-enabled false
gsettings set org.gnome.desktop.session idle-delay 0
gsettings set org.gnome.desktop.screensaver lock-enabled false
gsettings set org.gnome.settings-daemon.plugins.power idle-dim false

EOF
sudo chmod 777 "$HOME"/bin/screen-saver-off.sh
printf "[Desktop Entry]\nEncoding=UTF-8\nName=LG-Screen\nGenericName=LiquidGalaxy screen-saver-off\nComment=This script turns screen-saver off\nExec=bash $HOME/bin/screen-saver-off.sh\nTerminal=false\nOnlyShowIn=GNOME\nType=Application\nStartupNotify=false\nX-GNOME-Autostart-enabled=true\n" > $HOME"/.config/autostart/lg-screen-saver-off.desktop"



# Launch with 'liquidgalaxy' command
if ! grep -Fq "liquidgalaxy" ~/.bashrc
then
    echo "alias liquidgalaxy='bash $HOME/bin/startup-script.sh'" >> ~/.bashrc
    source ~/.bashrc
    # Chromebooks launch on terminal
    #printf "echo -n \"Do you want to initialize LiquidGalaxy (y/n)? \"\nread answer\nif [ \"\$answer\" != \"\${answer#[Yy]}\" ] ;then\nliquidgalaxy\nfi\n" >> ~/.bashrc
fi

if ! grep -Fq "setxkbmap" ~/.bashrc
then
    printf "setxkbmap es\n" >> ~/.bashrc
    source ~/.bashrc
fi

# Add lg user sudo permissions (NOPASSWD) for ~/bin/startup-script.sh
echo 'lg ALL=(ALL) NOPASSWD: /home/lg/bin/startup-script.sh' | sudo tee -a /etc/sudoers
echo 'lg ALL=(ALL) NOPASSWD: /home/lg/bin/ip-reloader.sh' | sudo tee -a /etc/sudoers
echo 'lg ALL=(ALL) NOPASSWD: /sbin/ip addr add*' | sudo tee -a /etc/sudoers
echo 'lg ALL=(ALL) NOPASSWD: /etc/init.d/ssh restart' | sudo tee -a /etc/sudoers
echo 'lg ALL=(ALL) NOPASSWD: /etc/init.d/squid restart' | sudo tee -a /etc/sudoers
echo 'lg ALL=(ALL) NOPASSWD: /etc/init.d/apache2 restart' | sudo tee -a /etc/sudoers
echo 'lg ALL=(ALL) NOPASSWD: /sbin/iptables*' | sudo tee -a /etc/sudoers
echo 'lg ALL=(ALL) NOPASSWD: /usr/bin/tee*' | sudo tee -a /etc/sudoers
echo 'lg ALL=(ALL) NOPASSWD: /sbin/iptables-restore*' | sudo tee -a /etc/sudoers
echo 'lg ALL=(ALL) NOPASSWD: /usr/sbin/tcpdump*' | sudo tee -a /etc/sudoers

# Web interface
if [ $IS_MASTER == true ]; then
	echo "Installing web interface (master only)..."
	sudo apt-get -yq install php php-cgi libapache2-mod-php
	sudo touch /etc/apache2/httpd.conf
	sudo sed -i '/accept.lock/d' /etc/apache2/apache2.conf
	sudo rm /var/www/html/index.html
	sudo cp -r $GIT_FOLDER_NAME/php-interface/. /var/www/html/
	sudo chown -R $LOCAL_USER:$LOCAL_USER /var/www/html/

	sudo systemctl enable apache2
fi

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

# Cleanup
sudo rm -r $GIT_FOLDER_MAIN

#
# Global cleanup
#
echo "Cleaning up..."
sudo apt-get -yq autoremove

echo "Liquid Galaxy installation completed! :-)"
echo "Press ENTER key to exit"
read
gnome-session-quit --no-prompt
