cd "$(dirname "$0")" || exit
# 获取当前文件夹未提交文件
modified=$(git status -s ./ | grep -E 'M|A')
if [ -n "$modified" ]; then
  export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11.0.9.jdk/Contents/Home
  mvn clean -Dmaven.test.skip=true package install
fi