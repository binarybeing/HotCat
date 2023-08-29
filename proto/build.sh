origin_dir=$(pwd)
cd "$(dirname "$0")" || exit
# 获取当前文件夹未提交文件

function do_build() {
    modified=$(git status -s ./ | grep -E 'M|A')
    if [ -n "$modified" ]; then
      export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_301.jdk/Contents/Home/jre
      mvn clean -Dmaven.test.skip=true package install
      sh src/main/resources/python_grpc.sh
    fi
}
do_build || echo "do proto build failed"
cd "$origin_dir" || exit



