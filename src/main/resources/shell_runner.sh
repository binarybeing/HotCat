 script_path="$1"
 cd "$script_path"
 port="$2"
 event_id="$3"
 log_file="$event_id.log"
 python3 "$script_path" "$port" "$event_id" "$script_path" > "$log_file" 2>&1
 cat "$log_file"
 rm "$log_file"
 sleep 3

