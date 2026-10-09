"""Convert a Mojang-named access transformer to SRG names using MDG's namedToIntermediate.tsrg.
Usage: at2srg.py <named.cfg> <namedToIntermediate.tsrg>  (prints the SRG AT; comments name the Mojang member)"""
import sys,re
at,tsrg=sys.argv[1],sys.argv[2]
methods={}; fields={}
cls=None
for line in open(tsrg):
    if not line.startswith('\t'):
        p=line.split(); cls=p[0] if p else None; continue
    p=line.strip().split()
    if len(p)==3: methods[(cls,p[0],p[1])]=p[2]
    elif len(p)==2: fields[(cls,p[0])]=p[1]
for line in open(at):
    s=line.rstrip('\n')
    m=re.match(r'^(\S+) (\S+) (\w+)(\(.*)?$',s)
    if not s or s.startswith('#') or not m:
        print(s); continue
    acc,c,name,desc=m.groups(); ci=c.replace('.','/')
    if name=='<init>' or not name: print(s); continue
    if desc:
        srg=methods.get((ci,name,desc))
        if not srg: sys.exit('no mapping for '+s)
        print(f'{acc} {c} {srg}{desc} # {name}' if srg!=name else s)
    else:
        srg=fields.get((ci,name))
        if not srg: sys.exit('no mapping for '+s)
        print(f'{acc} {c} {srg} # {name}' if srg!=name else s)
