#!/usr/bin/python
# -*- coding:UTF-8 -*-
'''
Copyright 2009 http://code.google.com/p/pytoolkits/. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
  * Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above
    copyright notice, this list of conditions and the following
    disclaimer in the documentation and/or other materials provided
    with the distribution.
  * Neither the name of http://code.google.com/p/pytoolkits/ nor the names of its
    contributors may be used to endorse or promote products derived
    from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
'''
"""
Functions that provide transparent read-only access to dictzipped files
"""

import struct, sys
import zlib
import __builtin__

FTEXT, FHCRC, FEXTRA, FNAME, FCOMMENT = 1, 2, 4, 8, 16

READ, WRITE = 1, 2

def write32(output, value):
    output.write(struct.pack("<l", value))

def write32u(output, value):
    output.write(struct.pack("<L", value))

def read32(input):
    return struct.unpack("<l", input.read(4))[0]

def open(filename, mode="rb", compresslevel=9):
    return DictzipFile(filename, mode, compresslevel)

class DictzipFile:
    """
    """
    myfileobj = None

    def __init__(self, filename=None, mode=None, 
                 compresslevel=9, fileobj=None, cachesize=2):
        if fileobj is None:
            fileobj = self.myfileobj = __builtin__.open(filename, mode or 'rb')
        if filename is None:
            if hasattr(fileobj, 'name'): filename = fileobj.name
            else: filename = ''
        if mode is None:
            if hasattr(fileobj, 'mode'): mode = fileobj.mode
            else: mode = 'rb'

        if mode[0:1] == 'r':
            self.mode = READ
            self.filename = filename
        else:
            raise ValueError, "Mode " + mode + " not supported"

        self.fileobj = fileobj
        self._read_gzip_header()
        self.pos = 0
        self.cachesize = cachesize
        self.cache = {}
        self.cachekeys = []

    def __repr__(self):
        s = repr(self.fileobj)
        return '<dictzip ' + s[1:-1] + ' ' + hex(id(self)) + '>'

    def _read_gzip_header(self):
        magic = self.fileobj.read(2)
        if magic != '\037\213':
            raise IOError, 'Not a gzipped file'
        method = ord( self.fileobj.read(1) )
        if method != 8:
            raise IOError, 'Unknown compression method'
        flag = ord( self.fileobj.read(1) )
        # modtime = self.fileobj.read(4)
        # extraflag = self.fileobj.read(1)
        # os = self.fileobj.read(1)
        self.fileobj.read(6)

        if flag & FEXTRA:
            # Read the extra field
            xlen=ord(self.fileobj.read(1))
            xlen=xlen+256*ord(self.fileobj.read(1))
            extra = self.fileobj.read(xlen)           
            while 1:
                l = ord(extra[2])+256*ord(extra[3])
                e = extra[:4+l]
                if e[:2]<>'RA':
                    extra=extra[4+l:]
                    if not extra:
                        raise "Missing dictzip extension"
                    continue
                else:
                    break
            length = ord(extra[2])+256*ord(extra[3])
            ver = ord(extra[4])+256*ord(extra[5])
            self.chlen = ord(extra[6])+256*ord(extra[7])
            chcnt = ord(extra[8])+256*ord(extra[9])
            p = 10
            lens = []
            for i in xrange(chcnt):
                thischlen = ord(extra[p])+256*ord(extra[p+1])
                p = p+2
                lens.append(thischlen)
            chpos = 0
            self.chunks = []
            for i in lens:
                self.chunks.append( (chpos, i) )
                chpos = chpos+i
            self._lastpos = chpos
        else:
            raise "Missing dictzip extension"

        if flag & FNAME:
            # Read and discard a null-terminated string containing the filename
            while (1):
                s=self.fileobj.read(1)
                if not s or s=='\000': break
        if flag & FCOMMENT:
            # Read and discard a null-terminated string containing a comment
            while (1):
                s=self.fileobj.read(1)
                if not s or s=='\000': break
        if flag & FHCRC:
            self.fileobj.read(2)     # Read & discard the 16-bit header CRC

        self._firstpos = self.fileobj.tell()

    def write(self,data):
        raise ValueError, "write() not supported on DictzipFile object"

    def writelines(self,lines):
        raise ValueError, "writelines() not supported on DictzipFile object"

    def _readchunk(self,n):
        if n>=len(self.chunks):
            return ''
        if self.cache.has_key(n):
            return self.cache[n]
        self.fileobj.seek(self._firstpos+self.chunks[n][0])
        s = self.fileobj.read(self.chunks[n][1])
        dobj = zlib.decompressobj(-zlib.MAX_WBITS)
        output = dobj.decompress(s)
        del dobj
        #self.cache = {} # crude hack until proper cache is done
        self.cache[n] = output
        self.cachekeys.append(n)
        # delete the oldest filled up item in cache
        if len(self.cachekeys) > self.cachesize:
            try:
                del self.cache[self.cachekeys[0]]
                del self.cachekeys[0]
            except KeyError:
                pass
        return output

    def read(self, size=-1):
        firstchunk = self.pos/self.chlen
        offset = self.pos - firstchunk*self.chlen
        if size == -1:
            lastchunk = len(self.chunks)+1
            finish = 0
            npos = sys.maxint
        else:
            lastchunk = (self.pos+size)/self.chlen
            finish = offset+size
            npos = self.pos+size
        buf = ""
        for i in range(firstchunk, lastchunk+1):
            buf = buf+self._readchunk(i)
        r = buf[offset:finish]
        self.pos = npos
        return r

    def close(self):
        self.fileobj.close()

    def __del__(self):
        self.close()

    def flush(self):
        pass

    def seek(self, pos, whence=0):
        if whence == 0:
            self.pos = pos
        elif whence == 1:
            self.pos = self.pos+pos
        elif whence == 2:
            raise "Seeking from end of file not supported"
            # fixme

    def tell(self):
        return self.pos

    def isatty(self):
        return 0

    def readline(self, size=-1):
        if size < 0: size = sys.maxint
        bufs = []
        orig_size = size
        oldpos = self.pos
        readsize = min(100, size)    # Read from the file in small chunks
        while 1:
            if size == 0:
                return ''.join(bufs) # Return resulting line

            c = self.read(readsize)
            i = c.find('\n')
            if i>=0:
                self.pos = self.pos-len(c)+i+1
            if size is not None:
                # We set i=size to break out of the loop under two
                # conditions: 1) there's no newline, and the chunk is 
                # larger than size, or 2) there is a newline, but the
                # resulting line would be longer than 'size'.
                if i==-1 and len(c) > size: i=size-1
                elif size <= i: i = size -1

            if i >= 0 or c == '':
                bufs.append(c[:i+1])    # Add portion of last chunk
                return ''.join(bufs) # Return resulting line

            # Append chunk to list, decrease 'size',
            bufs.append(c)
            size = size - len(c)
            readsize = min(size, readsize * 2)

    def readlines(self, sizehint=0):
        # Negative numbers result in reading all the lines
        if sizehint <= 0: sizehint = sys.maxint
        L = []
        while sizehint > 0:
            line = self.readline()
            if line == "": break
            L.append( line )
            sizehint = sizehint - len(line)
        return L
    
    def extract(self,out,size):
        self.seek(0)
        data=self.read(size)
        print len(data)
        out.write(data)
        out.flush()
        self.seek(0)
    
    def runtest(self):
		print self.chlen;
		print self._firstpos
		
if __name__ == '__main__':
	pass
