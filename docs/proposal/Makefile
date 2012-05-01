SOURCES := slides.tex
TARGET := presentation

$(TARGET): $(SOURCES)
	pdflatex -jobname $(TARGET) slides.tex

draft: $(SOURCES)
	pdflatex -draftmode -jobname $(TARGET) slides.tex


clean:
	rm -f $(TARGET).pdf $(TARGET).log $(TARGET).toc $(TARGET).aux $(TARGET).nav $(TARGET).out $(TARGET).snm $(TARGET).vrb
