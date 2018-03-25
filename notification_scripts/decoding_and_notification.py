from __future__ import division
import numpy as np
from matplotlib import pyplot as plt
import scipy.io.wavfile as wav
from numpy.lib import stride_tricks
from pyfcm import FCMNotification

file_name = "announcement.wav"

API_Key = "AAAAnwZ5EsA:APA91bHATVu6_6E2msLVSkEMThEjFV-EF5v46YxJasDOoiSKHJepvLXM1mAWqS5DvegDD-GeGff3wHIIBI4Fa9eNmDzfsmx_GUT_aK2izfhf9ZHNJRDemiLU0IFcsxeyOollVxeassGj"
registration_id = "eAKJmc3xIPU:APA91bF_Texdh9utHRPaKgK383nkyHwmpjlVnVl-BociO6Wj_tTxiERvnjAaQp7EOamzvNy7fe2l_fexppq7lZdLO3eMl-VxcFq28sMh0f9_zbnxn6jBTMNi50nholyuqQFIR-YEFvKr"


### short time fourier transform of audio signal ###
def stft(sig, frameSize, overlapFac=0.5, window=np.hanning):
    win = window(frameSize)
    hopSize = int(frameSize - np.floor(overlapFac * frameSize))
    
    # zeros at beginning (thus center of 1st window should be for sample nr. 0)
    samples = np.append(np.zeros(np.floor(frameSize/2.0)), sig)    
    # cols for windowing
    cols = np.ceil( (len(samples) - frameSize) / float(hopSize)) + 1
    # zeros at end (thus samples can be fully covered by frames)
    samples = np.append(samples, np.zeros(frameSize))
    
    frames = stride_tricks.as_strided(samples, shape=(cols, frameSize), strides=(samples.strides[0]*hopSize, samples.strides[0])).copy()
    frames *= win
    
    return np.fft.rfft(frames)    
    
### scale frequency axis logarithmically ###   
def logscale_spec(spec, sr=44100, factor=20.):
    timebins, freqbins = np.shape(spec)

    scale = np.linspace(0, 1, freqbins) ** factor
    scale *= (freqbins-1)/max(scale)
    scale = np.unique(np.round(scale))
    
    # create spectrogram with new freq bins
    newspec = np.complex128(np.zeros([timebins, len(scale)]))
    for i in range(0, len(scale)):
        if i == len(scale)-1:
            newspec[:,i] = np.sum(spec[:,scale[i]:], axis=1)
        else:        
            newspec[:,i] = np.sum(spec[:,scale[i]:scale[i+1]], axis=1)
    
    # list center freq of bins
    allfreqs = np.abs(np.fft.fftfreq(freqbins*2, 1./sr)[:freqbins+1])
    freqs = []
    for i in range(0, len(scale)):
        if i == len(scale)-1:
            freqs += [np.mean(allfreqs[scale[i]:])]
        else:
            freqs += [np.mean(allfreqs[scale[i]:scale[i+1]])]
    
    return newspec, freqs

def get_code_from_spectrogram( cols, timebins):
    present = [0]
    strech = 0
    max_strech = 0
    for i in range(1, timebins):
        if i in cols:
            present.append(1)
            strech += 1
        else:
            present.append(0)
            if strech:
                # print( strech )
                pass
            max_strech = max(strech, max_strech)
            strech = 0
    # print( "max_strech=", str(max_strech) )

    if 0:
        plt.plot( present )
    threshold = max_strech*0.5
    strech_count = 0
    buffer_interval = 0
    min_buffer = 100000000000000
    for i in range(0, timebins):
        if present[i]==1:
            strech_count += 1
            if buffer_interval:
                min_buffer = min(min_buffer, buffer_interval)
            buffer_interval = 0
        else:
            buffer_interval += 1
            if strech_count<threshold:
                for j in range(1, strech_count+1):
                    present[i-j] = 0
            strech_count = 0
    # print( "min_buffer=", str(min_buffer) )
    present = np.trim_zeros(present)
    if 0:
        plt.plot( present )
        axes = plt.gca()
        axes.set_xlim([0,len(present)])
        axes.set_ylim([0,2])

    codes = []
    interval = max_strech + min_buffer
    interval = interval - 14
    current_char = present[0]
    current_count = 1
    buff_leniency = 5
    for i in range(0, len(present)):
        if present[i]==current_char:
            current_count += 1
        else:
            # print(current_count)
            if current_char == 1:
                codes.append('1')
                # print(1)
            else:
                if current_count in range(min_buffer-buff_leniency, min_buffer+buff_leniency):
                    # print("ignored")
                    pass
                else:
                    current_count -= min_buffer
                    # print( current_count, current_count/interval )
                    zeros = int( round(current_count/interval) )
                    codes += ['0']*zeros
                    for i in range(0,zeros):
                        # print(0)
                        pass
            # reset
            current_char = present[i]
            current_count = 0
    codes.append('1')
    print(codes)
    print( len(codes) )
    return codes

def get_string_from_code( codes ):
    codes = codes[5:len(codes)-5]
    bin_lenth = 5

    string = ""
    while len(codes)!=0:
        current_dec = int(''.join(codes[0:bin_lenth]), base=2)
        codes = codes[5:]
        # print(current_dec)
        if current_dec:
            string += chr( current_dec + ord('a') - 1 )
        else:
            string += " "

    print(string)
    return string

### plot spectrogram ###
def decode_msg(audiopath, binsize=2**9, plotpath=None, colormap="jet"):
    samplerate, samples = wav.read(audiopath)
    s = stft(samples, binsize)
    
    sshow, freq = logscale_spec(s, factor=1.0, sr=samplerate)
    ims = 20.*np.log10(np.abs(sshow)/10e-6) # amplitude to decibel
    timebins, freqbins = np.shape(ims)
    print( "times=", str(timebins) )
    print( "freq=", str(freqbins) )

    # remove noise
    np.place(ims, ims<270, [120])
    
    # find freq start and end index
    start_index = np.min( np.where( np.int16(np.round(freq)) > 11950 ) )
    end_index = np.max( np.where( np.int16(np.round(freq)) < 12300 ) )
    print( "start=", str(start_index) )
    print( "end=", str(end_index) )
    
    # get cols and then code
    cols = np.where( ims[:,start_index] > 250)
    cols = cols[0]
    codes = get_code_from_spectrogram( cols, timebins)
    msg  = get_string_from_code( codes )

    # cut the region
    if 1:
        ims = ims[:, start_index:end_index]

    # plot the spectrogram
    if 0:
        plt.figure(figsize=(15, 7.5))
        plt.imshow(np.transpose(ims), origin="lower", aspect="auto", cmap=colormap)
        plt.colorbar()

        plt.xlabel("time (s)")
        plt.ylabel("frequency (hz)")
        plt.xlim([0, timebins-1])
        plt.ylim([0, freqbins])

        xlocs = np.float32(np.linspace(0, timebins-1, 5))
        plt.xticks(xlocs, ["%.02f" % l for l in ((xlocs*len(samples)/timebins)+(0.5*binsize))/samplerate])
        ylocs = np.int16(np.round(np.linspace(0, freqbins-1, 10)))
        plt.yticks(ylocs, ["%.02f" % freq[i] for i in ylocs])
    
        if plotpath:
            plt.savefig(plotpath, bbox_inches="tight")
        else:
            plt.show()
        
        plt.clf()

    return msg

### send app notification ###
def send_notification(message):
    push_service = FCMNotification( api_key=API_Key )
    message_title = "New Audio Announcement"
    message_body = message
    result = push_service.notify_single_device(registration_id=registration_id, message_title=message_title, message_body=message_body)
    print(result)

# plotstft("concat.wav")
msg = decode_msg( file_name )
send_notification(msg)
