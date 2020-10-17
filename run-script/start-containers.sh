#!/usr/bin/env bash
docker run -v hii-redis-data:/data --name hii-redis -d redis  --appendonly yes

cd hii-backend
docker rmi Image hii-backend:1.0
docker build -t hii-backend:1.0 .
docker run --rm -d -p 8080:8080 -p 8090:8090 -v hii-backend-log:/usr/src/app/log -e "env=production" --link hii-redis:redis --name hii-backend-app  hii-backend:1.0
echo 'Back end Done'
cd ..

cd hii-frontend
docker rmi Image hii-frontend:1.0
docker build -t hii-frontend:1.0 .
docker run --rm -d -v ${PWD}:/usr/src/app -p 3000:3000 --name hii-frontend-app hii-frontend:1.0
cd ..

