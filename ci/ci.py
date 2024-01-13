import os

# Compile Kotlin code
compile_command = "kotlinc ../src/Main.kt -include-runtime -d Main.jar"
os.system(compile_command)

# Initialize sum of outputs
total_sum = 0

# Iterate through input files 0000.txt to 0099.txt
for i in range(0, 100):
    input_file = f"in/{i:04d}.txt"
    output_file = f"out/t{i:04d}.txt"

    # Run Kotlin program for each input file
    run_command = f"CI=true java -jar Main.jar < {input_file} > {output_file}"
    os.system(run_command)

    # Read the output and add to the total sum
    with open(output_file, 'r') as f:
        output = int(f.read().strip())
        total_sum += output

# Print the total sum of outputs
print(f"{total_sum}")