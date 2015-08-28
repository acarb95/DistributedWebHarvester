test_home=~/cs455/HW2/src/
config=~/cs455/HW2/src/config

for i in `cat config`
do
    IFS=',' read -r host_port url <<< "$i"
    IFS=':' read -r host port <<< "$host_port"
  	echo 'logging into '${host}' with url ' ${url}
    gnome-terminal -x bash -c "ssh -t ${host} 'cd $test_home; java -classpath /s/bach/k/under/acarbona/cs455/HW2/lib/jericho-html-3.3.jar:. cs455.harvester.Crawler ${port} 10 ${url} $config;bash;'" &
done
