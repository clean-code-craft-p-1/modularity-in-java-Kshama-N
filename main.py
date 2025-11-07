import sys
import os

def process_batch(filename):
    try:
        f = open(filename, 'r')
    except FileNotFoundError:
        print("Error: File not found.")
        return

    lines = f.readlines()
    f.close()

    temps = []
    timestamps = []
    errors = 0
    bad_lines = []

    for i, line in enumerate(lines):
        line = line.strip()
        if not line:
            continue
        parts = line.split(',')
        if len(parts) != 2:
            errors += 1
            bad_lines.append((i, line))
            continue

        timestamp, value = parts[0].strip(), parts[1].strip()

        # Validate timestamp
        if len(timestamp.split(':')) != 3:
            errors += 1
            bad_lines.append((i, line))
            continue

        try:
            temp = float(value)
        except ValueError:
            errors += 1
            bad_lines.append((i, line))
            continue

        # Drop impossible temperatures
        if temp < -100 or temp > 200:
            errors += 1
            bad_lines.append((i, line))
            continue

        temps.append(temp)
        timestamps.append(timestamp)

    if not temps:
        print("No valid temperature data found.")
        return

    # Calculate statistics
    max_temp = max(temps)
    min_temp = min(temps)
    avg_temp = sum(temps) / len(temps)

    # Print summary
    print("=" * 60)
    print("Temperature Analysis Summary")
    print("=" * 60)
    print(f"Total readings: {len(lines)}")
    print(f"Valid readings: {len(temps)}")
    print(f"Errors: {errors}")
    print("-" * 60)
    print(f"Max temperature: {max_temp:.2f}")
    print(f"Min temperature: {min_temp:.2f}")
    print(f"Average temperature: {avg_temp:.2f}")
    print("-" * 60)

    # Print invalid lines (verbose)
    if errors > 0:
        print("Invalid lines:")
        for idx, bad in bad_lines:
            print(f"  Line {idx + 1}: {bad}")

    # Save report
    out_name = filename + "_summary.txt"
    try:
        with open(out_name, 'w') as out:
            out.write("Temperature Analysis Summary\n")
            out.write("=" * 50 + "\n")
            out.write(f"File analyzed: {filename}\n")
            out.write(f"Total readings: {len(lines)}\n")
            out.write(f"Valid readings: {len(temps)}\n")
            out.write(f"Errors: {errors}\n")
            out.write(f"Max temperature: {max_temp:.2f}\n")
            out.write(f"Min temperature: {min_temp:.2f}\n")
            out.write(f"Average temperature: {avg_temp:.2f}\n")
            out.write("-" * 60 + "\n")
            if errors > 0:
                out.write("\nInvalid lines:\n")
                for idx, bad in bad_lines:
                    out.write(f"  Line {idx + 1}: {bad}\n")
        print(f"Report saved to {out_name}")
    except Exception as e:
        print("Error saving file:", e)


if __name__ == "__main__":
    # Generate test data file
    test_filename = "test_temps.csv"
    test_data = [
        "09:15:30,23.5",
        "09:16:00,24.1",
        "09:16:30,22.8",
        "09:17:00,25.3",
        "09:17:30,23.9",
        "09:18:00,24.7",
        "09:18:30,22.4",
        "09:19:00,26.1",
        "09:19:30,23.2",
        "09:20:00,25.0"
    ]
    
    with open(test_filename, 'w') as f:
        for line in test_data:
            f.write(line + "\n")
    
    print(f"Created test file: {test_filename}")
    
    # Process the test file
    process_batch(test_filename)
    
    # Verify the summary file was created (if user chose to save)
    summary_file = test_filename + "_summary.txt"
    if os.path.exists(summary_file):
        print(f"\nSummary file created: {summary_file}")
        with open(summary_file, 'r') as f:
            content = f.read()
            assert "Total readings: 10" in content
            assert "Valid readings: 10" in content
            assert "Errors: 0" in content
            print("✓ Summary file contents verified")
    
    # Clean up test files
    if os.path.exists(test_filename):
        os.remove(test_filename)
    if os.path.exists(summary_file):
        os.remove(summary_file)
