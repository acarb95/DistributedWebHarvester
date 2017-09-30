all: compile
	@echo -e '[INFO] Done!'
clean:
	@echo -e '[INFO] Cleaning Up..'
	@-rm -rf cs455/**/**/*.class cs455/**/*.class *~  Carbonari_Amanda_ASG2_PC.tar
 
compile: 
	@echo -e '[INFO] Compiling the Source..'
	@javac -classpath /s/bach/k/under/acarbona/cs455/HW2/lib/jericho-html-3.3.jar -d . cs455/**/**/*.java cs455/**/*.java

package:
	@echo -e '[INFO] Packaging the source..'
	@-tar cvf Carbonari_Amanda_ASG2_PC.tar cs455/**/**/*.java cs455/**/*.java Makefile ../lib/ *.sh config README
