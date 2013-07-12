paths
=====

/me
---
1 through 3 - anywhere there is a uid. note that "/me" is shorthand for "/:<uid of logged in user>";
8 through 9 - only if :uid is that of logged in user (i.e.

1.  /me/ -> user handle

    get: all of current user's info

2.  /me/nick

    get:

    put(string):

3.  /me/public (boolean) - whether or not the user is public

    get:

    put(boolean):

        true: other users can see my info etc, follows are autoapproved.

        false: only current followers can see anything, all requests require approval

4.  /me/info -> user info object

    get:

    put(json):

5.  /me/following -> user list

    get:

    post(:ruid): generates a follow request, which will be autoapproved if :ruid is public

    delete(:ruid): unfollows :ruid

6.  /me/followers -> user list

    get:

    delete(:ruid): gets rid of a follower (moves them to blocked?)

7.  /me/topics -> topic list

    get:

    post(:rtid): attempts to add the user to the topic, possibly generating an add request

    delete(:rtid): removes self from the topic

8.  /me/blocked -> user list

    get: users blocked by this user

    post(:ruid): adds :ruid to blocked users (can't be self)

    delete(:ruid): unblocks a user

9.  /me/tags -> tags list

    get: get the list

    post(:tagname): starts tracking tag

    delete(:tagname): stops tracking tag

10. /me/requests -> list of requests

11.

user
----
a subset of the endpoints of /me

1.  /:uid/ -> user handle

    get: everyone (will only return fields visible to logged in user)

2.  /:uid/nick

    get: everyone

3.  /:uid/public (boolean) - whether or not the user is public

    get: everyone

        true: other users can see my info etc, follows are autoapproved.

        false: only current followers can see anything, all requests require approval

4.  /:uid/info -> user info object

    get: if (public) everyone else :cid in followers

5.  /:uid/following -> user list

    get: public ? everyone | :uid followers - user list

6.  /:uid/followers -> user list

    get: public ? everyone | :uid followers - user list

7.  /:uid/topics -> topic list

    get: public ? everyone who can see topic | :uid followers who can see topic

8.  /:uid/request

topics
------
only visible if topic is public or user is participating:

1. /topics/:tid -> topic object {id:<tid>, op:<uid>, title:<string>}
2. /topics/:tid/title -> string
3. /topics/:tid/public -> boolean
4. /topics/:tid/info -> object
5. /topics/:tid/tags -> tags list
6. /topics/:tid/participants -> user list

messages
--------
1. /topics/:id/messages -> messages queue
2. /topics/:id/messages/:id -> get a message object

events
------
1. /events/ -> events queue (for currently logged-in user)
2. /events/:id/ -> get event at id

requests
--------
1. /requests/ -> request queue

tags
----
1. /tag/:string -> topic list (any public topic with this tag

