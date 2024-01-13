import os

# Compile Kotlin code
compile_command = "kotlinc ../src/Main.kt -include-runtime -d Main.jar"
os.system(compile_command)

# Initialize sum of outputs
total_sum = 0

run_command = f'seq 0 99 | xargs printf "%04d\n" | xargs -I@ -P8 sh -c "CI=true java -jar Main.jar < in/@.txt > out/@.txt"'
os.system(run_command)
for i in range(0, 100):
    output_file = f"out/{i:04d}.txt"

    # Read the output and add to the total sum
    with open(output_file, 'r') as f:
        output = int(f.read().strip())
        total_sum += output

# Print the total sum of outputs
print(f"{total_sum}")