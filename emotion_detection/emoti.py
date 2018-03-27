#!flask/bin/python
from flask import Flask, request, jsonify
import subprocess
app = Flask(__name__)
import os
import nltk
from nltk.sentiment.vader import SentimentIntensityAnalyzer
import re
import json
import requests
sid = SentimentIntensityAnalyzer()

mapper = {'neg': {'cheerful': 'warning', 'agressiv': 'anger', 'intoxicated': 'anger', 'nervous': 'neutral', 'neutral': 'neutral', 'tired': 'neutral'}, 'neutral': {'cheerful': 'neutral', 'agressiv': 'warning', 'intoxicated': 'neutral', 'nervous': 'neutral', 'neutral': 'neutral', 'tired': 'neutral'}, 'pos': {'cheerful': 'happy', 'agressiv': 'warning', 'intoxicated': 'neutral', 'nervous': 'neutral', 'neutral': 'neutral', 'tired': 'neutral'}}
anger = ["fed up", "shut up"]
@app.route('/')
def hello():
	return "Made with <3 by bagl"

@app.route('/analyze', methods=['POST'])
def analyze():
	file = request.files['audio/wav']
	string = request.form['description']
        print string
#        print file
	file.save('/home/aman/Downloads/opensmile/test_int.amr')
	os.system('ffmpeg -i test_int.amr -ar 22050 out.wav')
	output = subprocess.check_output(['./inst/bin/SMILExtract', '-C' , 'config/emobase_live4_batch_single.conf', '-I', 'out.wav'])
	os.system('rm -rf out.wav')
	ss = sid.polarity_scores(string)
	res =  os.popen('curl -d "text=' + string + '" http://text-processing.com/api/sentiment/').read()
	print res
	res = json.loads(res)
	max_ss = res['label']
#	max_ss = "neu"
#	max_ss_cnt = -1
#	for k in ss:
#		print str(k) + " " + str(ss[k])
#		if float(ss[k]) > max_ss_cnt and k != 'compound':
#			max_ss = k
#			max_ss_cnt = float(ss[k])
#	print max_ss
	tag = "happy"
	for line in output.split('\n'):
		print line
		if "NAME=abc_affect" in line:
			m = re.findall(r'CATEGORY=([a-z]+):', line)
			print "SENTIMENT: " + mapper[max_ss][m[0]]
			if "fed up" in string or "get out" in string:
				tag = "anger"
			else:
				tag = mapper[max_ss][m[0]]
	return jsonify({'text': string, 'tag': tag})

if __name__ == "__main__":
	app.run(host='0.0.0.0')
