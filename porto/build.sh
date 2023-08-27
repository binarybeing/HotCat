cd "$(dirname "$0")" || exit
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11.0.9.jdk/Contents/Home
mvn clean -Dmaven.test.skip=true package install