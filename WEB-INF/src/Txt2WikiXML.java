import java.io.*;
import java.util.*;
import org.jdom.*;
import org.jdom.output.*;

public class Txt2WikiXML {
	static final int tLLINK = 0;
	static final int tRLINK = 1;
	static final int tCOLON = 2;
	static final int tLSQ = 3;
	static final int tRSQ = 4;
	static final int tITALICS = 5;
	static final int tBOLD = 6;
	static final int tAMP = 7;
	static final int tSEMI = 8;
	static final int tBR = 9;
	static final int tTT1 = 10;
	static final int tTT2 = 11;
	static final int tHEADER = 12;
	static final int tRULE = 13;
	static final int tITEM = 14;
	static final int tWHITE = 15;
	static final int tWORD = 16;
	static final int tEOF = 17;
	static final int tERROR = 18;

	int tLine;
	int tCol;
	String tWord;
	int tKind;

	public Txt2WikiXML(Reader in) {
		this.in = new BufferedReader(in);
		tLine = 1;
		tCol = 0;
		tWord = "";
		chars = new StringBuffer();
		nextChar();
	}

	void printToken() {
		switch (tKind) {
		case tLLINK:
			System.out.println("" + tLine + ":" + tCol + " " + "LLINK");
			break;
		case tRLINK:
			System.out.println("" + tLine + ":" + tCol + " " + "RLINK");
			break;
		case tCOLON:
			System.out.println("" + tLine + ":" + tCol + " " + "COLON");
			break;
		case tLSQ:
			System.out.println("" + tLine + ":" + tCol + " " + "LSQ");
			break;
		case tRSQ:
			System.out.println("" + tLine + ":" + tCol + " " + "RSQ");
			break;
		case tITALICS:
			System.out.println("" + tLine + ":" + tCol + " " + "ITALICS");
			break;
		case tBOLD:
			System.out.println("" + tLine + ":" + tCol + " " + "BOLD");
			break;
		case tAMP:
			System.out.println("" + tLine + ":" + tCol + " " + "AMP");
			break;
		case tSEMI:
			System.out.println("" + tLine + ":" + tCol + " " + "SEMI");
			break;
		case tBR:
			System.out.println("" + tLine + ":" + tCol + " " + "BR");
			break;
		case tTT1:
			System.out.println("" + tLine + ":" + tCol + " " + "TT1");
			break;
		case tTT2:
			System.out.println("" + tLine + ":" + tCol + " " + "TT2");
			break;
		case tHEADER:
			System.out.println("" + tLine + ":" + tCol + " " + "HEADER");
			break;
		case tRULE:
			System.out.println("" + tLine + ":" + tCol + " " + "RULE");
			break;
		case tITEM:
			System.out.println("" + tLine + ":" + tCol + " " + "ITEM");
			break;
		case tWHITE:
			System.out.println("" + tLine + ":" + tCol + " " + "WHITE");
			break;
		case tWORD:
			System.out.println("" + tLine + ":" + tCol + " " + "WORD='" + tWord
					+ "'");
			break;
		case tEOF:
			System.out.println("" + tLine + ":" + tCol + " " + "EOF");
			break;
		case tERROR:
			System.out.println("" + tLine + ":" + tCol + " " + "ERROR");
			break;
		}
	}

	BufferedReader in;
	int c = 0;

	int nextChar() {
		chars.append((char) c);
		try {
			c = in.read();
			// handle \r and \r\n as \n
			if (c == '\r') {
				c = '\n';
				in.mark(1);
				if (in.read() != '\n') {
					in.reset();
				}
			}
		} catch (Exception e) {
			c = -1;
		}
		if (c == '\n') {
			tLine++;
			tCol = 0;
		} else
			tCol++;
		return c;
	}

	boolean nameMode = false;
	boolean urlMode = false;

	boolean isWhite(int c) {
		return (c == ' ' || c == '\n' || c == '\r');
	}

	boolean allowed(int c) {
		if (c == -1)
			return false;
		if (nameMode && c != '_' && !('a' <= c && c <= 'z')
				&& !('A' <= c && c <= 'Z'))
			return false;
		if (c == ' ')
			return false;
		if (c == '\n')
			return false;
		if (c == '\r')
			return false;
		if (!urlMode && c == '[')
			return false;
		if (c == ']')
			return false;
		if (!urlMode && c == ':')
			return false;
		if (!urlMode && c == '&')
			return false;
		if (!urlMode && c == ';')
			return false;
		if (!urlMode && c == '<')
			return false;
		if (!urlMode && c == '-')
			return false;
		if (!urlMode && c == '*')
			return false;
		if (!urlMode && c == '=')
			return false;
		if (!urlMode && c == '\'')
			return false;
		return true;
	}

	String nextWord() {
		StringBuffer s = new StringBuffer();
		while (allowed(c)) {
			s.append((char) c);
			nextChar();
		}
		return s.toString();
	}

	boolean newlines = true;

	StringBuffer chars;

	int nextToken() {
		chars = new StringBuffer();
		switch (c) {
		case '[':
			nextChar();
			if (c == '[') {
				tKind = tLLINK;
				nextChar();
				break;
			}
			tKind = tLSQ;
			break;
		case ']':
			nextChar();
			if (c == ']') {
				tKind = tRLINK;
				nextChar();
				break;
			}
			tKind = tRSQ;
			break;
		case ':':
			tKind = tCOLON;
			nextChar();
			break;
		case '&':
			tKind = tAMP;
			nextChar();
			break;
		case ';':
			tKind = tSEMI;
			nextChar();
			break;
		case '<':
			nextChar();
			if (c == 'b') {
				nextChar();
				if (c == 'r') {
					nextChar();
					if (c == '/') {
						nextChar();
						if (c == '>') {
							tKind = tBR;
							nextChar();
						} else {
							tWord = "<br/";
							tKind = tWORD;
						}
					} else {
						tWord = "<br";
						tKind = tWORD;
					}
				} else {
					tWord = "<b";
					tKind = tWORD;
				}
			} else if (c == 't') {
				nextChar();
				if (c == 't') {
					nextChar();
					if (c == '>') {
						nextChar();
						tKind = tTT1;
					} else {
						tWord = "<tt";
						tKind = tWORD;
					}
				} else {
					tWord = "<t";
					tKind = tWORD;
				}
			} else if (c == '/') {
				nextChar();
				if (c == 't') {
					nextChar();
					if (c == 't') {
						nextChar();
						if (c == '>') {
							tKind = tTT2;
							nextChar();
						} else {
							tWord = "</tt";
							tKind = tWORD;
						}
					} else {
						tWord = "</t";
						tKind = tWORD;
					}
				} else {
					tWord = "</";
					tKind = tWORD;
				}
			} else {
				tWord = "<";
				tKind = tWORD;
			}
			break;
		case '\r':
			if (newlines)
				tKind = tWHITE;
			else
				tKind = tERROR;
			nextChar();
			break;
		case '\n':
			if (newlines)
				tKind = tWHITE;
			else
				tKind = tERROR;
			nextChar();
			break;
		case ' ':
			tKind = tWHITE;
			while (c == ' ')
				nextChar();
			break;
		case '\'':
			nextChar();
			if (c == '\'') {
				nextChar();
				if (c == '\'') {
					tKind = tBOLD;
					nextChar();
				} else {
					tKind = tITALICS;
				}
			} else {
				tWord = "'";
				tKind = tWORD;
			}
			break;
		case '-':
			if (tCol == 1) {
				nextChar();
				if (tCol == 2 && c == '-') {
					nextChar();
					if (tCol == 3 && c == '-') {
						nextChar();
						if (tCol == 4 && c == '-') {
							tKind = tRULE;
							nextChar();
						} else {
							tWord = "---";
							if (c != -1 && !isWhite(c))
								tWord = tWord + (char) c;
							tKind = tWORD;
						}
					} else {
						tWord = "--";
						if (c != -1 && !isWhite(c))
							tWord = tWord + (char) c;
						tKind = tWORD;
					}
				} else {
					tWord = "-";
					if (c != -1 && !isWhite(c))
						tWord = tWord + (char) c;
					tKind = tWORD;
				}
			} else {
				tWord = "-";
				tKind = tWORD;
				nextChar();
			}
			break;
		case '*':
			if (tCol == 1) {
				tKind = tITEM;
			} else {
				tWord = "*";
				tKind = tWORD;
			}
			nextChar();
			break;
		case '=':
			nextChar();
			if (c == '=') {
				tKind = tHEADER;
				nextChar();
			} else {
				tWord = "=";
				if (c != -1 && !isWhite(c))
					tWord = tWord + (char) c;
				tKind = tWORD;
			}
			break;
		case -1:
			tKind = tEOF;
			break;
		default:
			tWord = nextWord();
			if (tWord.length() == 0)
				tKind = tERROR;
			else
				tKind = tWORD;
			break;
		}
		return tKind;
	}

	void skip() {
		while (tKind == tWHITE)
			nextToken();
	}

	void nextName() {
		nameMode = true;
		nextToken();
		nameMode = false;
	}

	void nextURL() {
		urlMode = true;
		nextToken();
		urlMode = false;
	}

	Namespace wns = Namespace.getNamespace("http://cs.au.dk/dWebTek/WikiXML");

	void addWord(Element e, String w) {
		w.trim();
		if (w.length() == 0)
			return;
		List contents = e.getContent();
		int size = contents.size();
		if (size > 0) {
			Element last = (Element) contents.get(size - 1);
			if (last.getName().equals("text")) {
				String s = last.getText();
				last.setText(s + w);
				return;
			}
		}
		e.addContent((new Element("text", wns)).setText(w));
	}

	void addWhite(Element e) {
		List contents = e.getContent();
		int size = contents.size();
		if (size > 0) {
			Element last = (Element) contents.get(size - 1);
			if (!last.getName().equals("ws")
					&& !last.getName().equals("header")
					&& !last.getName().equals("rule")
					&& !last.getName().equals("list")
					&& !last.getName().equals("item")
					&& !last.getName().equals("br")) {
				e.addContent(new Element("ws", wns));
				return;
			}
		}
	}

	void trim(Element e) {
		List contents = e.getContent();
		int size = contents.size();
		if (size > 0) {
			Element last = (Element) contents.get(size - 1);
			if (last.getName().equals("ws")) {
				e.removeContent(last);
			}
		}
	}

	boolean blockHeader = false;
	boolean blockItalics = false;
	boolean blockBold = false;
	boolean blockITEM = false;
	boolean blockTT1 = false;
	boolean allowTT2 = false;

	void parseSequence(Element e) {
		while (true) {
			String name;
			String url;
			if (tKind == tWORD) {
				addWord(e, tWord);
				nextToken();
			} else if (tKind == tWHITE) {
				addWhite(e);
				nextToken();
			} else if (tKind == tLLINK) {
				nextName();
				if (tKind != tWORD) {
					addWord(e, "[[");
				} else {
					name = tWord;
					nextToken();
					if (name.equals("Image")) {
						if (tKind != tCOLON) {
							addWord(e, "[[Image");
						} else {
							nextURL();
							if (tKind != tWORD) {
								addWord(e, "[[Image:");
							} else {
								e.addContent((new Element("image", wns))
										.setAttribute("url", tWord));
								nextToken();
								skip();
								if (tKind == tRLINK)
									nextToken();
							}
						}
					} else {
						skip();
						if (tKind == tCOLON) {
							nextName();
							e.addContent((new Element("wikilink", wns))
									.setAttribute("wiki", name).setAttribute(
											"word", tWord));
							nextToken();
							skip();
							if (tKind == tRLINK)
								nextToken();
						} else {
							e.addContent((new Element("wikilink", wns))
									.setAttribute("word", name));
							if (tKind == tRLINK)
								nextToken();
						}
					}
				}
			} else if (tKind == tLSQ) {
				nextURL();
				url = tWord;
				newlines = false;
				nextToken();
				if (tKind != tWHITE) {
					addWord(e, "[" + url);
				} else {
					newlines = true;
					nextName();
					e.addContent((new Element("link", wns)).setAttribute("url",
							url).setAttribute("word", tWord));
					nextToken();
					skip();
					if (tKind == tRSQ)
						nextToken();
				}
			} else if (tKind == tITALICS) {
				if (blockItalics) {
					trim(e);
					return;
				}
				newlines = false;
				nextToken();
				Element italics = new Element("italics", wns);
				blockItalics = true;
				parseSequence(italics);
				blockItalics = false;
				if (italics.getContent().size() > 0)
					e.addContent(italics);
				newlines = true;
				if (tKind == tITALICS)
					nextToken();
				newlines = true;
			} else if (tKind == tHEADER) {
				if (blockHeader) {
					trim(e);
					return;
				}
				newlines = false;
				nextToken();
				Element header = new Element("header", wns);
				blockHeader = true;
				parseSequence(header);
				blockHeader = false;
				if (header.getContent().size() > 0)
					e.addContent(header);
				newlines = true;
				if (tKind == tHEADER)
					nextToken();
				newlines = true;
			} else if (tKind == tBOLD) {
				if (blockBold) {
					trim(e);
					return;
				}
				newlines = false;
				nextToken();
				Element bold = new Element("bold", wns);
				blockBold = true;
				parseSequence(bold);
				blockBold = false;
				if (bold.getContent().size() > 0)
					e.addContent(bold);
				newlines = true;
				if (tKind == tBOLD)
					nextToken();
			} else if (tKind == tAMP) {
				nextName();
				if (tKind != tWORD) {
					addWord(e, "&" + chars.toString());
					nextToken();
				} else {
					e.addContent((new Element("character", wns)).setAttribute(
							"entity", tWord));
					nextToken();
					if (tKind == tSEMI)
						nextToken();
				}
			} else if (tKind == tBR) {
				e.addContent(new Element("br", wns));
				nextToken();
			} else if (tKind == tRULE) {
				e.addContent(new Element("rule", wns));
				nextToken();
			} else if (!blockTT1 && tKind == tTT1) {
				nextToken();
				blockTT1 = true;
				Element tt = new Element("tt", wns);
				allowTT2 = true;
				parseSequence(tt);
				blockTT1 = false;
				allowTT2 = false;
				if (tt.getContent().size() > 0)
					e.addContent(tt);
				skip();
				if (tKind == tTT2)
					nextToken();
			} else if (tKind == tITEM) {
				if (blockITEM) {
					trim(e);
					return;
				}
				newlines = false;
				Element list = new Element("list", wns);
				while (tKind == tITEM) {
					nextToken();
					Element item = new Element("item", wns);
					blockITEM = true;
					parseSequence(item);
					blockITEM = false;
					list.addContent(item);
					newlines = true;
					if (tKind != tITEM)
						nextToken();
					newlines = false;
				}
				e.addContent(list);
				newlines = true;
			} else if (allowTT2 && tKind == tTT2) {
				trim(e);
				return;
			} else if (tKind == tEOF) {
				trim(e);
				return;
			} else if (tKind == tERROR) {
				trim(e);
				return;
			} else {
				addWord(e, chars.toString());
				nextToken();
			}
		}
	}

	public Document parseWiki() {
		nextToken();
		Element wiki = new Element("wiki", wns);
		parseSequence(wiki);
		return new Document(wiki);
	}

	public static void main(String[] args) {
		try {
			Txt2WikiXML t2w = new Txt2WikiXML(new FileReader(args[0]));
			Document wiki = t2w.parseWiki();
			XMLOutputter xout = new XMLOutputter();
			xout.output(wiki, System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}