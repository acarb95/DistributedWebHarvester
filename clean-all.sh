config=~/cs455/HW2/src/config

for i in `cat config`
do
    IFS=',' read -r host_port url <<< "$i"
    IFS=':' read -r host port <<< "$host_port"
  	echo 'logging into '${host}' with url ' ${url}
    gnome-terminal -x bash -c "ssh -t ${host} 'cd /tmp; rm cs455-acarbona/ -r -f;'" &
done
