paths
=====

/me
---
1.  /me/

    get: user object of current user

    put: (reserved for authentication updating)

    post: (reserved for session initiation)

    delete: (reserved for session termination)

2.  /me/nick

    get: current nick

    put(string): new nick

3.  /me/public (boolean) - whether or not the user is public

    get:

    put(boolean):

        true: other users can see my info etc, follows are autoapproved.

        false: only current followers can see anything, all requests require approval

4.  /me/info

    get: current info object

    put(json): replaces it

5.  /me/following

    get: list of user ids (or maybe user objects?)

6.  /me/following/:uid

    get: do i follow :ruid?

    put: attempt to follow :uid --- /user/:ruid/public
    
        true: insert :uid into the set

        false: deny (either that or generated a follow request)

    delete: unfollow :uid

7.  /me/followers

    get: returns the list

8.  /me/followers/:uid

    get: boolean - does :uid follow me

    delete: removes :uid from followers

9.  /me/blocked

    get: the list of blocked users

10. /me/blocked/:uid

    get: is :uid blocked?

    put: blocks :uid (unfollows, removes from following, autodenies all requests)

    delete: unblocks :uid

11. /me/topics

    get: list of topic ids of topics participating in

12. /me/topics/:tid

    get: am I participating in :tid

    put: attempt to join :tid

    delete: stop participating in :tid

13. /me/tags

    get: list of tags i follow

14. /me/tags/:tagname

    get: do I track :tagname

    put: starts tracking :tagname (will see public postTopic events from the tag)

    delete: stops tracking :tagname

15. /me/requests

    get: list of (open!) requests that have been sent to me

16. /me/requests/:rid

    get: return the description of submitted request :rqid

17. /me/requests/accepted

    get: all the requests sent to me that I've accepted

18. /me/requests/accepted/:rid

    get: did I accept :rid?

    put: if :rid is open, accept it (which allows the changes it specifies)

19. /me/requests/rejected

    get: list of rejected requests

20. /me/requests/rejected/:rid

    get: did I reject :rid?

    put: if :rid is open, reject it (preventing its update)

user
----
1.  /user/

    get: server rules for new users

    post(user creation request object): requests the creation of a new user

1.  /user/:uid/

    get: will only return fields visible to logged in user

2.  /user/:uid/nick

    get: a string

3.  /user/:uid/public

    get: is :uid publically visible

4.  /user/:uid/info

    get: :uid's info object --- /:uid/public || /me/following/:uid

5.  /user/:uid/following

    get: list of users :uid follows --- /:uid/public || /me/following/:uid

6.  /user/:uid/following/:uid2

    get: is :uid following :uid2? --- (/:uid/public || /me/following/:uid) && (/:uid2/public || /me/following/:uid2)

7.  /user/:uid/followers

    get: list of users following :uid

8.  /user/:uid/followers/:uid2

    get: is :uid followed by :uid2? --- (/:uid/public || /me/following/:uid) && (/:uid2/public || /me/following/:uid2)

9.  /user/:uid/topics

    get: list of topics :uid participates in (that /me can see) --- /:uid/public  || /me/following/:uid

10. /user/:uid/topics/:tid

    get: is :uid participating in :tid? --- (/:uid/public || /me/following/:uid) && (/topic/:tid/public || /me/topics/:tid)

11. /user/:uid/requests

    post(request object): posts the request to user --- !( {:uid}/me/blocked/

topics
------

1.  /topic/

    get: (reserved for topic config object)

    post(initial topic object): creates a new topic, returns the url

2.  /topic/:tid

    get: topic object (only fields visible to /me)

3.  /topic/:tid/title

    get: title string --- /topic/:tid/public || /me/topics/:tid

    put(string): update title --- (/me/id == /topic/:tid/op) || (/topic/:tid/sops/@/me/id)

4.  /topic/:tid/public

    get: is :tid public? --- (all)

    put(boolean): update publicness --- (/me/id == /topic/:tid/op) || (/topic/:tid/sops/@/me/id)

5.  /topic/:tid/info

    get: info object --- /topic/:tid/public || /me/topics/:tid

    put(info object): update info object --- (/me/id == /topic/:tid/op) || /topic/:tid/sops/@/me/id

6.  /topic/:tid/tags (only if topic is public)

    get: list of tags --- /topic/:tid/public

7.  /topic/:tid/tags/:tagname

    get: is :tid tagged with :tagname? --- /topic/:tid/public

    put: add :tagname to :tid - /topic/:tid/public && (/me/id == /topic/:tid/op || /topic/:tid/sops/@/me/id)

8.  /topic/:tid/op

    get: returns the uid of the op (original poster, lead operator) --- /topic/:tid/public || /me/topics/:tid

9.  /topic/:tid/sops

    get: list of sops (second ops) --- /me/topics/:tid

10. /topic/:tid/sops/:uid

    get: is :uid a second op for :tid? --- /me/topics/:tid

    put: add :uid to the sops list --- /me/id == /topic/:tid/op

    delete: remove :uid from sops --- /me/id == /topic/:tid/op

11. /topic/:tid/participating

    get: list of uids participating in the topic (only ones i can see) --- /topic/:tid/public || /me/topic/:tid

12. /topic/:tid/participating/:uid

    get: is :uid participating in :tid? --- /me/topics/:tid || (/topic/:tid/:public && /user/:tid/public)

    put: add :uid to topic --- (/me/id == :uid && /topic/:tid/public)

    delete: remove :uid from topic --- :uid == /me/id || /me/id == /topic/:tid/op || (/topic/:tid/sops/@/me/id && :uid != /topic/:tid/op && !/topic/:tid/sops/:uid)

13. /topic/:tid/requests

    get: list of open requests --- /me/id == /topic/:tid/op || /topic/:tid/sops/@/me/id

    post(topic request object): sends request to topic --- anyone

14. /topic/:tid/requests/:rid

    get: return the description of submitted request :rqid --- /me/id == /topic/:tid/op || /topic/:tid/sops/@/me/id

15. /topic/:tid/requests/accepted

    get: all the requests sent to :tid that've been accepted --- /me/id == /topic/:tid/op || /topic/:tid/sops/@/me/id

16. /topic/:tid/requests/accepted/:rid

    get: did :tid accept :rid? --- /me/id == /topic/:tid/op || /topic/:tid/sops/@/me/id

    put: if :rid is open, accept it (which allows the changes to :tid it specifies) --- /me/id == /topic/:tid/op || /topic/:tid/sops/@/me/id

17. /topic/:tid/requests/rejected

    get: list of rejected requests --- /me/id == /topic/:tid/op || /topic/:tid/sops/@/me/id

18. /topic/:tid/requests/denied/:rid

    get: did :tid reject :rid? --- /me/id == /topic/:tid/op || /topic/:tid/sops/@/me/id

    put: if :rid is open, reject it (preventing its update) --- /me/id == /topic/:tid/op || /topic/:tid/sops/@/me/id

messages
--------
1.  /topic/:tid/message

    get: same as ./first/1 --- /me/topics/:tid

    post(message object): posts new message --- /me/topics/:tid

2.  /topic/:tid/message/first[/:count]

    get: first :count (default and min 1) messages in :tid --- /me/topics/:tid

3.  /topic/:tid/message/last[/:count]

    get: last :count messages in :tid --- /me/topics/:tid

4.  /topic/:tid/message/at/:mid[/:count]

    get: (inclusive before) :count messages ending at :mid --- /me/topics/:tid

5.  /topic/:tid/message/before/:mid[/:count]

    get: (exclusive at) :count (at least 1) messages that came before :mid --- /me/topics/:tid

6.  /topic/:tid/message/from/:mid[/:count]

    get: (inclusive after) :count messages (sequentially) from :mid to now --- /me/topics/:tid

7.  /topic/:tid/message/after/:mid[/count]

    get: (exclusive from) :count messages that came after :mid --- /me/topics/:tid

events
------
1.  /event/ (a user specific resource)

    get: same as ./last/1

2.  /event/last[/:count]

    get: last :count events for user

3.  /event/at/:eid[/:count]

    get: (inclusive before) :count events ending at :eid

4.  /event/before/:eid[/:count]

    get: (exclusive at) :count (at least 1) events that came before :eid

5.  /event/from/:eid[/:count]

    get: (inclusive after) :count events (sequentially) from :eid towards now

6.  /event/after/:eid[/count]

    get: (exclusive from) :count events that came after :eid

tags
----
(tags are a view on a set of events involving public topics and a tag string)

1.  /tagged/:tagname

    get: same as ./last/1

2.  /tagged/:tagname/last[/:count]

    get: last :count public topics posted to :tagname

3.  /tagged/:tagname/at/:eid[/:count]

    get: (inclusive before) :count events ending at :eid

4.  /tagged/:tagname/before/:eid[/:count]

    get: (exclusive at) :count (at least 1) events that came before :eid

5.  /tagged/:tagname/from/:eid[/:count]

    get: (inclusive after) :count events (sequentially) from :eid towards now

6.  /tagged/:tagname/after/:eid[/count]

    get: (exclusive from) :count events that came after :eid
