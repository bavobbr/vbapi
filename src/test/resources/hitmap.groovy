import forum.utils.HitMap

HitMap<String> hitmap = new HitMap<String>()
hitmap.addHits("ten",4)
hitmap.addHits("ten",6)
hitmap.addHits("three",3)
hitmap.addHit("one")
hitmap.addHits("two",2)
println hitmap
println hitmap.asMap()
println hitmap.toSortedMap().getClass()
