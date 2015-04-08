# pdf-helper
A small tool hacked together to help with pdfs, mainly concerning printing. Seriously, don't expect a polished quality project. Use at your own risk.

# Mission Statement
I had to print a pdf without background images. The author didn't utilize layers in the file, so I had to remove certain images from the file myself.

This tool was hacked together in an afternoon to achieve that goal.

I hope that someone finds it usefuly. And hopefully I, or someone else, finds time to polish this.

# Image removal

Run the tool from the command line, like this:

```
java -jar pdfboxhelper-0.0.1-SNAPSHOT-jar-with-dependencies.jar -Xmx8000m
```

The tool will now ask what file to modify, and to what file to save the result. Don't overwrite the original...

Then the tool asks how often to save the results. This maybe useful in volatile circumstances, but for small files I'd recommend "at the end" option.

The tool will ask of every image it comes across if you want to remove or retain it in the document. The preview panel does its best to show the image. If ever in doubt, retain.

Do note that the tool will use __a lot__ of memory! Please provide a large number for the parameter _-Xmx_!

Also an __important__ note: There is no error logging, other than to standard output stream. That means that unless running from the commandline, you won't know immediately if the tool is working really hard _or_ just crashed.

## Improvement suggestions

The tool doesn't actually _remove_ the images, it just sets their height to 0. This is because I remember reading somewhere that in the pdf spec the same image stream could be used somewhere else.. But I can't remember, I should brush up on the pdf spec some day.

The tool doesn't compress the file. This can lead to situations where an imageless pdf file is larger than the original, which sure will confuse at first.

The tool uses a lot of memory, which is a bummer. My "ye olde" laptop can't handle it, so that's one thing I'd like to improve personally.

And the user experience isn't fun at all. Instead of just iterating over each image, a page/resource navigator should be shown to the user, from which to select images to hide/show, with live previews and _stuff_. One can dream, eh :)

# Shoulders of giants

This tool is made possible with the use of [Apache PDFBox](https://pdfbox.apache.org/). Check it out. Many operations have ready command line tools, and using it in your Java projects is easy (as demonstrated by this tool right here).
