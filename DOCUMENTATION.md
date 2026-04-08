# 📚 Phoenix Finance - Documentation Guide

Welcome to Phoenix Finance! This guide helps you quickly find what you need.

## 🚀 Start Here

**New to the project?** Start with [README.md](README.md) for an overview.

**Want to run it quickly?** Follow [QUICK_START_MORTGAGE.md](QUICK_START_MORTGAGE.md) (5 minutes).

**Ready to deploy?** See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md).

---

## 📋 All Documentation Files

| Document | Purpose | Read Time | Audience |
|----------|---------|-----------|----------|
| **[README.md](README.md)** | Project overview & quick start | 2 min | Everyone |
| **[QUICK_START_MORTGAGE.md](QUICK_START_MORTGAGE.md)** | 5-minute setup guide | 5 min | First-time users |
| **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** | How to deploy to production | 20 min | DevOps/Admins |
| **[MORTGAGE_LOAN_GUIDE.md](MORTGAGE_LOAN_GUIDE.md)** | Mortgage features reference | 15 min | Users/Developers |
| **[PAYOFF_ACCELERATOR_GUIDE.md](PAYOFF_ACCELERATOR_GUIDE.md)** | Accelerated payoff guide | 10 min | End users |
| **[CLEANUP_SUMMARY.md](CLEANUP_SUMMARY.md)** | Project cleanup details | 5 min | Developers |

---

## 🎯 Find What You Need

### "I want to run the app right now"
👉 [QUICK_START_MORTGAGE.md](QUICK_START_MORTGAGE.md)

### "I want to deploy to production"
👉 [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)

### "How do I use mortgage features?"
👉 [MORTGAGE_LOAN_GUIDE.md](MORTGAGE_LOAN_GUIDE.md)

### "How do I pay off my mortgage faster?"
👉 [PAYOFF_ACCELERATOR_GUIDE.md](PAYOFF_ACCELERATOR_GUIDE.md)

### "What changed in the project?"
👉 [CLEANUP_SUMMARY.md](CLEANUP_SUMMARY.md)

### "General project information"
👉 [README.md](README.md)

---

## 🏗️ Project Structure

```
Phoenix Finance (Java EE / Jakarta EE 8)
├── Investment Management
├── Property Bond Management  
├── Investor Portfolio Management
├── Mortgage Loan Management (NEW)
├── Payoff Acceleration (NEW)
└── RESTful API
```

---

## 💻 Quick Commands

### Build
```bash
cd phoenix_investment_finance
.\mvnw.cmd clean package -DskipTests
```

### Deploy with Docker (Recommended)
```bash
docker-compose build
docker-compose up -d
# Access: http://localhost:8080/phoenix_investment_finance/
```

### Deploy to Local WildFly
```bash
copy target\phoenix_investment_finance.war %JBOSS_HOME%\standalone\deployments\
```

See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for details.

---

## 🔄 Documentation Flow

```
Start Here (README.md)
    ↓
Quick Setup (QUICK_START_MORTGAGE.md)
    ↓
Choose Your Path:
├─→ Deploy (DEPLOYMENT_GUIDE.md)
├─→ Learn Features (MORTGAGE_LOAN_GUIDE.md)
└─→ Explore Payoff (PAYOFF_ACCELERATOR_GUIDE.md)
```

---

## 📊 File Sizes & Read Times

| File | Size | Read Time | Density |
|------|------|-----------|---------|
| README.md | ~1.5 KB | 2 min | Quick |
| QUICK_START_MORTGAGE.md | ~4 KB | 5 min | Quick |
| DEPLOYMENT_GUIDE.md | ~12 KB | 20 min | Comprehensive |
| MORTGAGE_LOAN_GUIDE.md | ~15 KB | 15 min | Detailed |
| PAYOFF_ACCELERATOR_GUIDE.md | ~18 KB | 10 min | Guide |
| CLEANUP_SUMMARY.md | ~10 KB | 5 min | Technical |

---

## 🎓 Learning Paths

### Path 1: "I just want to run it"
1. README.md (2 min)
2. QUICK_START_MORTGAGE.md (5 min)
3. Done! ✅

### Path 2: "I want to understand everything"
1. README.md (2 min)
2. QUICK_START_MORTGAGE.md (5 min)
3. MORTGAGE_LOAN_GUIDE.md (15 min)
4. PAYOFF_ACCELERATOR_GUIDE.md (10 min)
5. Total: 32 minutes

### Path 3: "I need to deploy this"
1. README.md (2 min)
2. DEPLOYMENT_GUIDE.md (20 min)
3. Done! ✅

### Path 4: "I'm a developer"
1. README.md (2 min)
2. CLEANUP_SUMMARY.md (5 min)
3. DEPLOYMENT_GUIDE.md (20 min)
4. Source code review
5. Total: ~30 minutes + code review

---

## 🔗 Important Links

- **Source Code**: `phoenix_investment_finance/` directory
- **Compiled Application**: `phoenix_investment_finance/target/phoenix_investment_finance.war`
- **Docker Configuration**: `docker-compose.yml`, `Dockerfile`
- **Database**: PostgreSQL 12+
- **Server**: WildFly 26+
- **Framework**: Jakarta EE 8

---

## ❓ FAQ

**Q: How do I start?**  
A: Read [README.md](README.md) then follow [QUICK_START_MORTGAGE.md](QUICK_START_MORTGAGE.md)

**Q: How do I deploy?**  
A: See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)

**Q: What are the new features?**  
A: Check [MORTGAGE_LOAN_GUIDE.md](MORTGAGE_LOAN_GUIDE.md)

**Q: How do I use the payoff accelerator?**  
A: Read [PAYOFF_ACCELERATOR_GUIDE.md](PAYOFF_ACCELERATOR_GUIDE.md)

**Q: What was cleaned up?**  
A: See [CLEANUP_SUMMARY.md](CLEANUP_SUMMARY.md)

---

## 📞 Support

For issues or questions:
1. Check the relevant guide above
2. Review source code in `phoenix_investment_finance/src/main/`
3. Check deployment logs in Docker or WildFly

---

**Happy coding!** 🎉

*Last updated: April 8, 2026*
