#!/usr/bin/env python3

# Parsing patterns taken from tuja-vortaro
# https://github.com/sstangl/tuja-vortaro

import gzip
import shutil

def build_etymology():
    print('opening etymology.txt')
    etymology_in = open('etymology.txt', 'r')
    etymology_out = gzip.open('etymology.bin', 'w')

    print('parsing etymology...')
    for line in etymology_in:
        if '[' in line:
            word, ety = line.strip().split('[')
            ety = '[' + ety
        else:
            word, ety = line.strip().split('=')

        word = word.strip()
        ety = ety.strip()

        etymology_out.write('{}:{}\n'.format(word, ety).encode())

    etymology_in.close()
    etymology_out.close()
    print('done')


def build_espdic():
    print('opening espedic.txt')
    # open the espdic file
    espdic_in = open('espdic.txt', 'r')
    espdic_out = gzip.open('espdic.bin', 'w')
    # read the header
    espdic_in.readline()

    print('parsing espdic...')
    # now let's parse the file into the database
    for line in espdic_in:
        es, en = line.strip().split(' : ')
       
        espdic_out.write('{}:{}\n'.format(es, en).encode())
        
    espdic_in.close()
    espdic_out.close()
    print('done')


def build_transitive():
   print('opening transitiveco.txt')
   transitive_in = open('transitiveco.txt', 'r')
   transitive_out = gzip.open('transitiveco.bin', 'w')

   print('parsing transitiveco...')
   for line in transitive_in:
       verb, trans = line.strip().split(' ')

       transitive_out.write('{}:{}\n'.format(verb, trans).encode())

   transitive_in.close()
   transitive_out.close()
   print('done')


def move_files():
   print('moving files into android project')
   for outputFile in ['espdic.bin', 'etymology.bin', 'transitiveco.bin']:
      shutil.move(outputFile, '../app/src/main/assets/' + outputFile)


def main():
   build_etymology()
   build_espdic()
   build_transitive()
   move_files()

main()
