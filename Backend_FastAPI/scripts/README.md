# Scripts

`MadCamp_Profiles/update_notion_profiles.py` synchronizes the `interests` and `bio` fields for every Notion user whose name is listed under `MadCamp_Profiles/Profile_json`.

```bash
cd Backend_FastAPI
source venv/bin/activate
python MadCamp_Profiles/update_notion_profiles.py
```

It assumes you have a running PostgreSQL instance reachable via the environment variables in `.env`.
