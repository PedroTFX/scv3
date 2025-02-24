#!/bin/bash

# Directory to watch (default: current directory)
WATCH_DIR=${1:-$(pwd)}
CLIENT_ARGS="127.0.0.1:12345 admin admin"

echo "Watching for .java file changes in: $WATCH_DIR"

restart_server_client() {
    echo "Change detected, recompiling..."

    # Find and compile only modified Java files
    find "$WATCH_DIR" -name "*.java" -print0 | xargs -0 javac

    if [ $? -ne 0 ]; then
        echo "Compilation failed! Fix errors before restarting."
        return
    fi

    pkill -f "java"  # Kills any running Java processes

    echo "Starting Server..."
    java Server &  # Run Server in the background

    # Give the server a short time to start (adjust if necessary)
    sleep 1

    echo "Starting Client..."
    java Client $CLIENT_ARGS # Run Client in the background

}

# Watch only for .java changes, ignoring .class files
fswatch -o --event Updated --event Created --event Renamed --exclude ".*\\.class$" "$WATCH_DIR" | while read -r change; do
    sleep 0.5  # Short delay to group events
    restart_server_client
done
