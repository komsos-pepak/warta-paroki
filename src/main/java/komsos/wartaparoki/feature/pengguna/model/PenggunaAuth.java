package komsos.wartaparoki.feature.pengguna.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import komsos.wartaparoki.feature.hakAkses.HakAkses;
import komsos.wartaparoki.feature.pengguna.Pengguna;
import komsos.wartaparoki.feature.peran.Peran;

public class PenggunaAuth implements UserDetails {
    private Pengguna user;
	
	public PenggunaAuth(Pengguna user) {
		this.user = user;
	}


	@Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<Peran> roles  = user.getPeran();
        return getGrantedAuthorities(getPrivileges(roles));
    }

    private List<String> getPrivileges(Collection<Peran> roles) {
        List<String> hakAksesList = new ArrayList<>();
        for (Peran role : roles) {
            for (HakAkses hakAkses : role.getHakAkses()) {
                if (!hakAksesList.contains(hakAkses.getKode())) {
                    hakAksesList.add(hakAkses.getKode());
                }
            }
        }
        return hakAksesList;
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : permissions) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return !user.getIsLocked();
	}


    public Pengguna getUser() {
        return user;
    }
}
