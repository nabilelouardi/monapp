FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src
COPY MonApp.slnx global.json ./
COPY src/MonApp.Api/MonApp.Api.csproj src/MonApp.Api/
RUN dotnet restore src/MonApp.Api/MonApp.Api.csproj
COPY . .
RUN dotnet publish src/MonApp.Api/MonApp.Api.csproj -c Release -o /app --no-restore

FROM mcr.microsoft.com/dotnet/aspnet:10.0-alpine AS runtime
WORKDIR /app
COPY --from=build /app .
USER app
EXPOSE 8080
ENV ASPNETCORE_URLS=http://+:8080
ENTRYPOINT ["dotnet", "MonApp.Api.dll"]
