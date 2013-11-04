package metadata.parsers;

import metadata.MetadataParserDelegate;

public class RadioClassicParser extends RadioJazzParser{	

	public RadioClassicParser(MetadataParserDelegate delegate)
	{
		super(delegate, "192", "http://www.cultandart.ru/radioclassic/play_classic.xml");
	}
}
