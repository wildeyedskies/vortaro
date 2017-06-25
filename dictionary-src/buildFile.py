#!/usr/bin/env python3

# Parsing patterns taken from tuja-vortaro
# https://github.com/sstangl/tuja-vortaro

import gzip
import struct

def build_etymology():
    print('opening etymology.txt')
    etymology = open('etymology.txt', 'r')

    print('loading etymology into a dictionary...')
    ety_dict = {}
    for line in etymology:
        line = line.replace('"','\\"')

        if '[' in line:
            word, ety = line.strip().split('[')
            ety = '[' + ety
        else:
            word, ety = line.strip().split('=')

        word = word.strip()
        ety = ety.strip()

        ety_dict[word] = ety

    etymology.close()
    print('done')
    return ety_dict

def build_words(outputFile):
    print('opening espedic.txt')
    # open the espdic file
    espdic = open('espdic.txt', 'r')
    # read the header
    espdic.readline()

    ety_dict = build_etymology()

    print('writing ESPDIC to file...')
    # now let's parse the file into the database
    for line in espdic:
        es, en = line.strip().split(' : ')
       
        ety = ety_dict.get(es, '')

        output_bytes = '{}:{}:{}\n'.format(es, en, ety).encode()
        outputFile.write(output_bytes)
        
    espdic.close()
    print('done')

def main():
    outputFile = gzip.open('vortaro.bin', 'w')
    build_words(outputFile)
    outputFile.close()

main()
