sources       = $(wildcard *.tex)

documentname = ojts


single:
	latex $(documentname).tex

force:
	latex $(documentname).tex -interaction=nonstopmode || echo ""

pdfsingle:
	pdflatex $(documentname).tex

pdfforce:
	pdflatex $(documentname).tex -interaction=nonstopmode || echo ""


$(documentname)-hva.toc:
	perl -p -e 's/contentsline/cntline/;' $(documentname).toc >  $(documentname)-hva.toc

html:
	hevea -fix $(documentname).tex #&& ./imagen $(documentname) 

towebsite:
	cp style.css $(documentname).html ojts*.png $(documentname).pdf ../../web-site/doc/general && \
	cp graphics/*.png ../../web-site/doc/general/graphics/
