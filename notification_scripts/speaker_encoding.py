import time
from subprocess import call

msg = "train is late"

def get_bin_arr(character):
	bin_lenth = 5
	if character==" ":
		return ['0'] * bin_lenth
	if character=="delimiter":
		return ['1'] * bin_lenth

	val = ord(character) - ord("a") + 1
	bin_arr = list(bin(val)[2:])
	less = bin_lenth - len(bin_arr)
	buckets = ['0'] * less
	bin_arr = buckets + bin_arr
	return bin_arr

def msg_to_code(message):
	message_arr = list(message)
	final_code = get_bin_arr("delimiter")
	for char in message_arr:
		final_code = final_code + get_bin_arr( char )
	final_code += get_bin_arr("delimiter")
	count = 0
	for i in range(0, len(final_code)):
		if final_code[i] == '1':
			count += 1
	return final_code, count

def code_to_audio(code):
	freq_int = 0.2
	silence_interval = 0.1
	high_freq = 20000

	for i in code:
		if i=='1':
			call(["play", "-n", "-c1", "synth", str(freq_int), "sine", str(high_freq)])
		else:
			time.sleep(freq_int)
		time.sleep(silence_interval)

# generate high freq. audio first
[coded_msg, count] = msg_to_code(msg)
code_to_audio( coded_msg )
print( coded_msg )
print( len(coded_msg) )
# print( count ) # prints # of ones

# generate the text to speech
string_list = msg.split(" ")
call( ["say"] + string_list + ["-r", "100"] )
