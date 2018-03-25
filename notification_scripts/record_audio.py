from subprocess import call

file_name = "announcement.wav"
call( ["sox", "-d", file_name] )
