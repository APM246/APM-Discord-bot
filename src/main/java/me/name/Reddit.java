package me.name;

import me.name.exceptions.InvalidPeriodException;
import me.name.exceptions.InvalidSorterException;
import me.name.exceptions.SubredditDoesNotExistException;
import net.dean.jraw.*;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.*;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.BarebonesPaginator;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.pagination.Paginator;
import net.dean.jraw.references.SubredditReference;

import java.util.ArrayList;
import java.util.List;

public class Reddit
{
    private UserAgent userAgent;
    private Credentials credentials;
    private NetworkAdapter adapter;
    private RedditClient reddit;

    private final int DISCORD_CHAR_LIMIT = 2000;
    private final String reddit_username = System.getenv("reddit_username");
    private final String reddit_password = System.getenv("reddit_password");
    private final String client_id = System.getenv("reddit_client_id");
    private final String client_secret = System.getenv("reddit_client_secret");

    public Reddit()
    {
        userAgent = new UserAgent("desktop", "net.dean.awesomescript", "v0.1", reddit_username);
        credentials = Credentials.script(reddit_username, reddit_password, client_id, client_secret);
        adapter = new OkHttpNetworkAdapter(userAgent);
        reddit = OAuthHelper.automatic(adapter, credentials);
    }

    public String[] getPost(String subredditName) throws Exception
    {
        String[] args = subredditName.split(" ", 2);
        DefaultPaginator<Submission> posts;
        if (args.length == 1) posts = reddit.subreddit(subredditName).posts().build();
        else
        {
            String[] specifier_args = args[1].split(" ", 2);
            SubredditSort sorter;
            switch (specifier_args[0].toLowerCase()) 
            {
                case "top":
                    sorter = SubredditSort.TOP;
                    break;
                case "best":
                    sorter = SubredditSort.BEST;
                    break;
                case "new":
                    sorter = SubredditSort.NEW;
                    break;
                case "controversial":
                    sorter = SubredditSort.CONTROVERSIAL;
                    break;
                case "rising":
                    sorter = SubredditSort.RISING;
                    break;
                case "hot":
                    sorter = SubredditSort.HOT;
                    break;
                default:
                    throw new InvalidSorterException();
                }

            if (sorter.getRequiresTimePeriod()) 
            {
                TimePeriod period;
                if (specifier_args.length == 1) throw new InvalidPeriodException();
                switch (specifier_args[1].toLowerCase())
                {
                    case "all":
                        period = TimePeriod.ALL;
                        break;
                    case "day":
                        period = TimePeriod.DAY;
                        break;
                    case "hour":
                        period = TimePeriod.HOUR;
                        break;
                    case "month":
                        period = TimePeriod.MONTH;
                        break;
                    case "year":
                        period = TimePeriod.YEAR;
                        break;
                    case "week":
                        period = TimePeriod.WEEK;
                        break;
                    default:
                        throw new InvalidPeriodException();
                }

                posts = reddit.subreddit(args[0]).posts().sorting(sorter).timePeriod(period).build();
            }
            
            else posts = reddit.subreddit(args[0]).posts().sorting(sorter).build();
        }
        
            List<String[]> content = new ArrayList<>();
            Listing<Submission> list_of_posts = posts.next();

            // Throw error if subreddit does not exist
            if (list_of_posts.isEmpty()) throw new SubredditDoesNotExistException();

            for (Submission s : list_of_posts) {
                if (!s.isSelfPost()) 
                {
                    if (s.getUrl().contains("gfycat.com") || s.getUrl().contains(".gifv")) 
                    {
                        content.add(new String[] {s.getUrl(), "gif"});  
                    }

                    else if (s.getUrl().contains("i.imgur.com") || s.getUrl().contains("i.redd.it"))
                    {
                        content.add(new String[] {s.getUrl(), "https://www.reddit.com/" + s.getPermalink(), s.getTitle(), "photo"});
                    }
                }

                else 
                {
                    String title = s.getTitle();
                    String text = s.getSelfText();
                    // -18 because of asterisks and newline characters between title and text and "continued" message
                    if (text.length() + title.length() > DISCORD_CHAR_LIMIT) text = text.substring(0, DISCORD_CHAR_LIMIT - title.length() - 18)
                    + "\n(continued)"; 
                    content.add(new String[] {"**" + title + "**\n\n" + text, "text"});
                }
            }

            if (content.size() == 0) return null;
            int random_number = (int) (content.size()*Math.random());
            return content.get(random_number);
    }

    public String getComment(String subredditName) throws SubredditDoesNotExistException
    {
        try
        {
            SubredditReference subreddit = reddit.subreddit(subredditName);
            BarebonesPaginator.Builder<Comment> comments = subreddit.comments();
            BarebonesPaginator<Comment> built = comments.build();
            List<Comment> commentslist = built.accumulateMerged(1);

            // Throw error if subreddit does not exist
            if (commentslist.isEmpty()) throw new SubredditDoesNotExistException();

            return commentslist.get(0).getBody();
        }

        catch (NetworkException e)
        {
            throw new SubredditDoesNotExistException();
        }
    }

    public String searchSubreddits(String searchwords) 
    {
        try
        {
            List<SubredditSearchResult> list = reddit.searchSubredditsByName(searchwords);
            if (list.isEmpty()) throw new SubredditDoesNotExistException();

            String result= "The following subreddits were found:";

            for (SubredditSearchResult subreddit: list)
            {
                result += "\n" + subreddit.getName();
            }

            return result;
        }

        catch (Exception e)
        {
            return "No subreddits could be found based on this search query";
        }
    }

    public String[] guessCity() 
    {
        try 
        {
            DefaultPaginator<Submission> posts = reddit.subreddit("CityPorn").posts().limit(Paginator.RECOMMENDED_MAX_LIMIT).build();
            List<Submission> submissions = new ArrayList<>();
            Listing<Submission> page = posts.next();

            for (Submission s: page)
            {
                if (!s.isSelfPost() && s.getUrl().contains("i.redd.it"))
                {
                    submissions.add(s);
                }
            }

            int random_number = (int) (submissions.size()*Math.random());
            Submission question = submissions.get(random_number);
            return new String[] {question.getUrl(), question.getTitle().replace(",", "").replace(".", "").toLowerCase()};
        }
        catch (Exception e) {return null;}
    }

    public static void main(String[] args) throws Exception
    {
        Reddit main = new Reddit();
        //System.out.println(main.findComment("pcmasterrace"));
        //System.out.println(Arrays.toString(main.guessCity()));
        System.out.println(main.getPost("NSFW_GIF")[0].toString());
    }
}