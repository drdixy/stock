
# This Makefile needs GNU make
#
# copy this file to the directory where your eps figures are
# and simply run make in order to update the pdf's
#

# this does not work on Solaris with GNU make 3.77 (propably a bug)
EPS_FILES = $(wildcard *.eps)

PNG_FILES = $(wildcard *.png)

# use this instead
#EPS_FILES = $(shell dir *.eps)

PDFS := $(patsubst %.eps, %.pdf, $(EPS_FILES))
PDFS += $(patsubst %.png, %.pdf, $(PNG_FILES))
EPSS := $(patsubst %.png, %.eps, $(PNG_FILES))

default: $(PDFS) $(EPSS)

%.pdf: %.eps
	epstopdf $<

%.eps: %.png
	convert $< $@

.PHONY : clean

clean :
	-rm *.pdf
