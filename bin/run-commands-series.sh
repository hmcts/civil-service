#!/bin/bash

# Function to run a command and check its exit status
run_command() {
    "$@" # Run the command
    return $? # Return the exit status
}

# Initialize variables to track overall failure
failed=0
continue_on_failure=0

# Parse command-line options
while getopts "c" opt; do
    case $opt in
        c)
            continue_on_failure=1
            ;;
        *)
            echo "Usage: $0 [-c] command1 command2 ..."
            exit 1
            ;;
    esac
done

# Shift arguments to get commands
shift $((OPTIND - 1))

# Check if no commands are provided
if [ $# -eq 0 ]; then
    echo "No commands provided."
    exit 1
fi

# Iterate over all arguments (commands)
for cmd in "$@"; do
    # Use a shell to execute the command, so it can handle chains like '&&'
    run_command bash -c "$cmd"
    
    # Check if the command failed
    if [ $? -ne 0 ]; then
        echo "Command '$cmd' failed."
        failed=1 # Set the failed flag
        if [ $continue_on_failure -eq 0 ]; then
            break # Stop execution if not continuing on failure
        fi
    fi
done

# Exit with a failure status if any command failed
if [ $failed -ne 0 ]; then
    exit 1
fi

exit 0
