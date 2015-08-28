package cs455.harvester.task;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.htmlparser.jericho.Config;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.LoggerProvider;
import net.htmlparser.jericho.Source;
import cs455.harvester.Crawler;

public class CrawlTask implements Task, Runnable{
	private String url;
	private ArrayList<String> urls_to_send;
	private int recurrance;
	private final boolean isHandOff;
	private final String sender;
	private final String original_url;
	private final Crawler owner;
//	private final String log = "/s/bach/k/under/acarbona/cs455/HW2/logs/crawltask_log_" + System.currentTimeMillis();
//	private PrintWriter printer;
	
	public CrawlTask(String url, int recurrance, boolean handoff, String sender, String original_url, Crawler owner) {
		this.sender = sender;
		isHandOff = handoff;
		this.url = url;
		this.recurrance = recurrance;
		urls_to_send = new ArrayList<String>();
		this.original_url = original_url;
		this.owner = owner;
	}

	public synchronized void run() {
		//url = resolveRedirects(url);
		ArrayList<String> urls = generateURLList();
		recurrance++;
		for (int i = 0; i < urls.size(); i++) {
			String site = urls.get(i);
			if (!site.contains("#") && !site.contains("mailto") && !site.contains("https") && !site.contains("ftp") && !site.contains("@")) {
				if (site.contains("://") && !site.contains("http")) {
					// SKIP DA LINK
					continue;
				}
				if (site.contains("?")) {
					site = site.substring(0, site.indexOf("?"));
				}
				
				if (site.contains(" ") && !site.contains("http")) {
					site = site.replaceAll(" ", "%20");
				}
				
				if (site.contains("%7E")) {
					site = site.replaceAll("%7E", "~");
				} else if (site.contains("%7e")) {
					site = site.replaceAll("%7e", "~");
				}
				
				try {
					if (!new URI(site).isAbsolute()) {
						URI resolvedURI = new URI(url).resolve(site);
						site = resolvedURI.toString();
					}
				} catch (URISyntaxException e) {
					continue;
				}
				
				site = normalize(site);
				
				if(site.contains("colostate")){
					if (!site.contains("::") && site.contains("http://")) {
						//site = resolveRedirects(site);
						urls_to_send.add(site);
					}
				}
			}
		}
		//System.out.println(Thread.currentThread().getId() + ": All done! Went through " + urls.size() + " links");
	}
	

//	private String resolveRedirects(String site) {
//		HttpURLConnection con = null;
//		try {
//			con = (HttpURLConnection) (new URL(site).openConnection());
//			con.setInstanceFollowRedirects(false);
//			con.setConnectTimeout(10000);
//			con.setReadTimeout(10000);
//			con.connect();
//			int responseCode = con.getResponseCode();
//			if (responseCode == 301) {
//				return con.getHeaderField("Location");
//			} else if (responseCode == 302) {
//				String newLink = con.getHeaderField("Location");
//				if (!newLink.endsWith("/")) {
//					newLink = newLink + "/";
//				}
//				if (newLink.contains("http")) {
//					return newLink;
//				} else {
//					return site + newLink;
//				} 
//			} else {
//				return site;
//			}
//		} catch (MalformedURLException e) {
//		} catch (IOException e) {
//		} catch (ClassCastException e) {
//		} catch (IllegalArgumentException e) {
//		} catch (Exception e) {
//			System.out.println("Exception in resolve redirects: " + e.getMessage());
//		} finally {
//			if (con != null) {
//				con.disconnect();
//			}
//		}
//		
//		return site;
//	}
	
	public String normalize(String normalized) {
		 
        if (normalized == null) {
            return null;
        }
 
        // If the buffer begins with "./" or "../", the "." or ".." is removed.
        if (normalized.startsWith("./")) {
            normalized = normalized.substring(1);
        } else if (normalized.startsWith("../")) {
            normalized = normalized.substring(2);
        } else if (normalized.startsWith("..")) {
            normalized = normalized.substring(2);
        }
 
        // All occurrences of "/./" in the buffer are replaced with "/"
        int index = -1;
        while ((index = normalized.indexOf("/./")) != -1) {
            normalized = normalized.substring(0, index) + normalized.substring(index + 2);
        }
 
        // If the buffer ends with "/.", the "." is removed.
        if (normalized.endsWith("/.")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
 
        int startIndex = 0;
 
        // All occurrences of "/<segment>/../" in the buffer, where ".."
        // and <segment> are complete path segments, are iteratively replaced
        // with "/" in order from left to right until no matching pattern remains.
        // If the buffer ends with "/<segment>/..", that is also replaced
        // with "/".  Note that <segment> may be empty.
        while ((index = normalized.indexOf("/../", startIndex)) != -1) {
            int slashIndex = normalized.lastIndexOf('/', index - 1);
            if (slashIndex >= 0) {
                normalized = normalized.substring(0, slashIndex) + normalized.substring(index + 3);
            } else {
                startIndex = index + 3;
            }
        }
        if (normalized.endsWith("/..")) {
            int slashIndex = normalized.lastIndexOf('/', normalized.length() - 4);
            if (slashIndex >= 0) {
                normalized = normalized.substring(0, slashIndex + 1);
            }
        }
 
        // All prefixes of "<segment>/../" in the buffer, where ".."
        // and <segment> are complete path segments, are iteratively replaced
        // with "/" in order from left to right until no matching pattern remains.
        // If the buffer ends with "<segment>/..", that is also replaced
        // with "/".  Note that <segment> may be empty.
        while ((index = normalized.indexOf("/../")) != -1) {
            int slashIndex = normalized.lastIndexOf('/', index - 1);
            if (slashIndex >= 0) {
                break;
            } else {
                normalized = normalized.substring(index + 3);
            }
        }
        if (normalized.endsWith("/..")) {
            int slashIndex = normalized.lastIndexOf('/', normalized.length() - 4);
            if (slashIndex < 0) {
                normalized = "/";
            }
        }
 
        return normalized;
    }

	@Override
	public synchronized String getURL() {
		return url;
	}
	
	public String getOrigUrl() {
		return original_url;
	}
	
	public synchronized ArrayList<String> getURLs() {
		ArrayList<String> clone = new ArrayList<String>();
		
		for(String url: urls_to_send) {
			clone.add(url);
		}
		
		return clone;
	}
	
	public synchronized int getRecurrance() {
		return recurrance;
	}
	
	public boolean getHandOff() {
		return isHandOff;
	}
	
	public String getSender() {
		return sender;
	}
	
	private ArrayList<String> generateURLList() {
		ArrayList<String> urls = new ArrayList<String>();
		HttpURLConnection con = null;
        String temp = null;
		try {
			Config.LoggerProvider = LoggerProvider.DISABLED;
			
			con = (HttpURLConnection)(new URL(url).openConnection());
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);
			con.connect();
	
			InputStream is = con.getInputStream();
			
			temp = con.getURL().toString();

            if (temp.contains("notfound")) {
                owner.addBrokenLink(url, original_url);
            } else {
			    url = temp;
			    // instead of passing the URL, pass the input stream.
			    Source source = new Source(is);
			
			    List<Element> aTags = source.getAllElements(HTMLElementName.A);
			
			    for (Element aTag : aTags) {
				    if (aTag.getAttributeValue("href") != null) {
					    urls.add(aTag.getAttributeValue("href"));
				    }
			    }
            }
		} catch (IOException e) {
            if (temp != null) {
                url = temp;
			    owner.addBrokenLink(url, original_url);
            } else {
			    owner.addBrokenLink(url, original_url);
            }
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		return urls;
	}

	@Override
	public int getType() {
		return TaskType.CRAWL_TASK;
	}
	
	public static void main (String[] args) {
		CrawlTask task = new CrawlTask("http://www.cs.colostate.edu/cstop/index.html", 0, false, null, null, null);
		long start = System.currentTimeMillis();
		task.run();
		long end = System.currentTimeMillis();
		System.out.println("Time took: " + (end - start));
		synchronized (task) {
			System.out.println(task.getURLs().size());
			System.out.println(Arrays.toString(task.getURLs().toArray()));
		}
	}

}
