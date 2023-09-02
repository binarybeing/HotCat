 script_path="$1"
 script_dir=$script_path
 if [ -d "$script_path" ]; then
    script_path="$script_path/__main__.py"
 fi
 if [ -f "$script_dir" ]; then
    script_dir=$(dirname "$script_path")
 fi
 cd "$script_dir" || exit 1
 port="$2"
 event_id="$3"
 json_param="$4"
 log_file="$event_id.log"
 python3 "$script_path" "$port" "$event_id" "$script_path" "$json_param" > "$log_file" 2>&1
 cat "$log_file"
 rm "$log_file"
 sleep 3

