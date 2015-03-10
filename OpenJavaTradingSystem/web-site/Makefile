
sources  = $(wildcard *.html)
sources += $(wildcard *.css)
sources += $(wildcard *.jpg)
sources += $(wildcard *.png)
sources += $(wildcard *.gif)
sources += $(shell find doc -type f ! -path "*CVS*" | xargs)

upload: ojts-site.tar.gz
	scp ojts-site.tar.gz cs224@ojts.sourceforge.net:/home/groups/o/oj/ojts/htdocs/ && \
	ssh cs224@ojts.sourceforge.net "cd /home/groups/o/oj/ojts/htdocs && tar -xzf ojts-site.tar.gz && rm ojts-site.tar.gz && chmod -R a+r *" && \
	touch upload

ojts-site.tar.gz: $(sources)
	tar -czvf ojts-site.tar.gz $(sources)

echo:
	echo $(sources)

clean:
	rm ojts-site.tar.gz
