package com.phoenixhell.auth.vo;

import lombok.Data;

@Data
public class GithubUserVo {

    /**
     * "login": "phoenixrever",
     * 	"id": 38639225,
     * 	"node_id": "MDQ6VXNlcjM4NjM5MjI1",
     * 	"avatar_url": "https://avatars.githubusercontent.com/u/38639225?v=4",
     */
    private String login;
    private String id;
    private String NodeId;
    private String AvatarUrl;
//    private String GravatarId;
//    private String url;
//    private String HtmlUrl;
//    private String FollowersUrl;
//    private String FollowingUrl;
//    private String GistsUrl;
//    private String StarredUrl;
//    private String SubscriptionsUrl;
//    private String OrganizationsUrl;
//    private String ReposUrl;
//    private String EventsUrl;
//    private String ReceivedEventsUrl;
//    private String type;
//    private boolean SiteAdmin;
//    private String name;
//    private String company;
//    private String blog;
//    private String location;
//    private String email;
//    private String hireable;
//    private String bio;
//    private String twitter_username;
//    private int PublicRepos;
//    private int PublicGists;
//    private int followers;
//    private int following;
//    private Date CreatedAt;
//    private Date UpdatedAt;
}
