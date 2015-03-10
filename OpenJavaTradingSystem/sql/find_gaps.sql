select (dir1.enddate + INTERVAL 1 DAY)  AS gapstart,
       (dir2.startdate - INTERVAL 1 DAY) AS gapend
from DataInputRegistry dir1, DataInputRegistry dir2
where dir1.observer = dir2.observer and dir1.subject = dir2.subject and dir1.market = dir2.market
  and (dir1.enddate + INTERVAL 1 DAY) = 
       (select max(dir3.enddate + INTERVAL 1 DAY)
        from DataInputRegistry dir3
        where dir1.observer = dir3.observer and dir1.subject = dir3.subject and dir1.market = dir3.market
          and dir3.enddate + INTERVAL 1 DAY <= dir2.startdate - INTERVAL 1 DAY));