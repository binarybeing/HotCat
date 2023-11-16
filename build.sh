cd "$(dirname "$0")" || exit
sh "proto/build.sh"

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.0.2.jdk/Contents/Home
./gradlew build -x test