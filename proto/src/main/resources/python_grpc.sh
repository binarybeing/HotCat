origin_dir=$(pwd)
cd "$(dirname "$0")" || (cd "$origin_dir" && exit)

# 判断文件夹src/main/generated/python是否存在，不存在则创建
if [ ! -d "../generated/python" ]; then
  mkdir -p ../generated/python
fi

cd ../proto || (cd "$origin_dir" && exit)
# 依赖本地安装 python相关依赖grpc，参考
# pip3 install grpcio
# pip3 install grpcio-tools

# shellcheck disable=SC2038
(find ./ -name '*.proto' | xargs python3 -m grpc_tools.protoc --proto_path=./ --python_out=../generated/python --grpc_python_out=../generated/python) || echo "proto build failed"

cd "$origin_dir" && exit
