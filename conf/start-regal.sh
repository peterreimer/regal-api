cd ~/development/thumby;
gnome-terminal --window-with-profile=play -e "/opt/activator/bin/activator \"run -Dhttp.port=9001\""

cd ~/development/etikett
gnome-terminal --window-with-profile=play -e "/opt/activator/bin/activator \"run -Dhttp.port=9002\""

cd ~/development/zettel
gnome-terminal --window-with-profile=play -e "/opt/activator/bin/activator \"run -Dhttp.port=9003\""


cd ~/development/regal-api
gnome-terminal --window-with-profile=play -e "/opt/activator/bin/activator \"run -Dhttp.port=9100\""

