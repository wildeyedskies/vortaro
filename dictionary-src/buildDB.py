#!/usr/bin/env python3

# Parsing patterns taken from tuja-vortaro
# https://github.com/sstangl/tuja-vortaro

import shutil
import sqlite3
import re
import sys

def build_etymology(conn):
    print('opening etymology.txt')
    etymology_in = open('etymology.txt', 'r')
    c = conn.cursor()

    print('parsing etymology...')
    for line in etymology_in:
        if '[' in line:
            word, ety = line.strip().split('[')
            ety = '[' + ety
        else:
            word, ety = line.strip().split('=')

        word = word.strip()
        ety = ety.strip()
        
        try:
            c.execute('''insert into ety values (?, ?)''', (word, ety))
        except sqlite3.IntegrityError:
            print('duplicate found for {}, ignoring'.format(word))

    etymology_in.close()
    conn.commit()
    c.close()
    print('done')


def build_espdic(conn):
    print('opening espedic.txt')
    # open the espdic file
    espdic_in = open('espdic.txt', 'r')
    c = conn.cursor()
    
    # read the header
    espdic_in.readline()

    print('parsing espdic...')
    # now let's parse the file into the database
    for line in espdic_in:
        if ':' not in line:
            continue

        eo, en = line.strip().split(' : ')

        # insert the Esperanto word
        c.execute('''insert into eo values (?)''', (eo,))
        eorow = c.lastrowid
        
        # some entries have es : (description in English)
        # this case needs to be handled specially
        if (en.startswith('(') and en.endswith(')')):
            c.execute('''insert into en values (?,?,?,?)''', (eorow, '', en, 0))
        else:
            # regex taken from https://stackoverflow.com/questions/26633452/how-to-split-by-commas-that-are-not-within-parentheses
            enlist = re.split(r',\s*(?![^()]*\))', en)
            for enword in enlist:
                # break apart the elaboration
                if enword.startswith('(') and enword != '()':
                    elbefore = 2
                    el = re.findall('\((.*?)\)', enword)[0]
                    enword = enword.split(')')[1].strip()
                elif enword.endswith(')') and enword != '()':
                    elbefore = 1
                    el = re.findall('\((.*?)\)', enword)[0]
                    enword = enword.split('(')[0].strip()
                else:
                    elbefore = 0
                    el = None

                c.execute('''insert into en values (?,?,?,?)''', (eorow, enword, el, elbefore))
        
    espdic_in.close()
    conn.commit()
    c.close()
    print('done')


def build_transitive(conn):
   print('opening transitiveco.txt')
   transitive_in = open('transitiveco.txt', 'r')
   c = conn.cursor()

   print('parsing transitiveco...')
   for line in transitive_in:
       verb, trans = line.strip().split(' ')
       
       c.execute('''insert into trans
       values (?, ?)''', (verb, 2 if trans == 't' else 1))

   transitive_in.close()
   conn.commit()
   c.close()
   print('done')


def move_files():
   print('moving files into android project')
   shutil.move('data.db', '../app/src/main/assets/data.db')

def create_tables(conn):
   c = conn.cursor()
   # etymology table
   c.execute('''create table ety
   (word text primary key, ety text) without rowid''')
   # transitive table
   c.execute('''create table trans
   (verb text primary key, trans integer) without rowid''')
   # Esperanto words
   c.execute('''create table eo
   (eoword text)''')
   # English words
   # Note that esindx refers to the rowid in the Esperanto table
   # el is the elaboration that may be before or after a word, denoted by elbefore
   c.execute('''create table en
   (eorow integer, enword text, el text, elbefore integer)''')
   # we index the english words because we also want to search on them
   c.execute('''create index enword on en (enword)''')
   c.execute('''create index eorow on en (eorow)''')
   
   # commit and close our transaction
   conn.commit()
   c.close()

def main():
   conn = sqlite3.connect('data.db')
   create_tables(conn)

   build_espdic(conn)
   build_etymology(conn)
   build_transitive(conn)
   move_files()

main()
