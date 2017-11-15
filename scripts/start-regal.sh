cd /opt/regal/thumby;
gnome-terminal --window-with-profile=play -e "/opt/activator/bin/activator \"run -Dhttp.port=9001\""

cd /opt/regal/etikett
gnome-terminal --window-with-profile=play -e "/opt/activator/bin/activator \"run -Dhttp.port=9002\""

cd /opt/regal/zettel
gnome-terminal --window-with-profile=play -e "/opt/activator/bin/activator \"run -Dhttp.port=9003\""


cd /opt/regal/regal-api
gnome-terminal --window-with-profile=play -e "/opt/activator/bin/activator \"run -Dhttp.port=9100\""

