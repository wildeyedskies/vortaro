#!/usr/bin/env python3

# Parsing patterns taken from tuja-vortaro
# https://github.com/sstangl/tuja-vortaro

import sqlite3
   
def build_words(c: sqlite3.Cursor):
    print('Creating words table')
    # drop any existing tables
    c.execute('''DROP TABLE IF EXISTS words''')
    # create the words table
    c.execute('''CREATE TABLE words
                (es TEXT, en TEXT)''')
    
    print('opening espedic.txt')
    # open the espdic file
    espdic = open('espdic.txt', 'r')
    # read the header
    espdic.readline()

    print('writing ESPDIC to database...', end=' ')
    # now let's parse the file into the database
    for line in espdic:
        line.replace('"', '\\"')
        es,en = line.strip().split(' : ')
        
        c.execute('INSERT INTO words VALUES (?,?)', (es, en))
    print('done')

    espdic.close()

def build_etymology(c: sqlite3.Cursor):
    print('Creating etymology table')

    c.execute('DROP TABLE IF EXISTS etymology')
    c.execute('''CREATE TABLE etymology
            (word TEXT, ety TEXT)''')

    print('opening etymology.txt')
    etymology = open('etymology.txt', 'r')
    
    print('writing etymology to database...', end=' ')
    for line in etymology:
        line = line.replace('"','\\"')

        if '[' in line:
            word, ety = line.strip().split('[')
            ety = '[' + ety
        else:
            word, ety = line.strip().split('=')

        word.strip()
        ety.strip()
        
        c.execute('INSERT INTO etymology VALUES (?,?)', (word, ety))
    print('done')

    etymology.close()

def main():
    print('Connecting to database')
    # create/connect to the database
    conn = sqlite3.connect('vortaro.db')
    c = conn.cursor()

    build_words(c)
    build_etymology(c)

    conn.commit()
    conn.close()

main()
