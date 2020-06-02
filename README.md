# Playing with scala - Slick

Source code for https://plippe.github.io/blog/2020/06/01/playing-with-scala-slick.html

```sh
-> curl localhost:9000/recipes
[]

-> curl localhost:9000/recipes -H 'Content-Type: application/json' -d '{"name":"pancakes","description":"fluffy"}' 
{"id":"4d1a1036-b4ff-4c5f-8f80-6ed6bd20c865","name":"pancakes","description":"fluffy"}

-> curl localhost:9000/recipes
[{"id":"4d1a1036-b4ff-4c5f-8f80-6ed6bd20c865","name":"pancakes","description":"fluffy"}]

-> curl localhost:9000/recipes/4d1a1036-b4ff-4c5f-8f80-6ed6bd20c865 -X DELETE

-> curl localhost:9000/recipes
[]
```
